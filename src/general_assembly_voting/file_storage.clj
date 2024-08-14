(ns general-assembly-voting.file-storage
  (:require
   [clojure.core.async :refer [>!!]]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [cognitect.aws.client.api :as aws]
   [cognitect.aws.credentials :as creds]
   [com.brunobonacci.mulog :as μ]
   [general-assembly-voting.env :as env]))

(def bucket (:bucket env/digital-ocean))

(def s3 (aws/client {:api :s3
                     :credentials-provider (creds/basic-credentials-provider
                                            {:access-key-id (:access-key env/digital-ocean)
                                             :secret-access-key (:secret-key env/digital-ocean)})
                     :endpoint-override {:hostname (:hostname env/digital-ocean)
                                         :protocol :https}
                     :region "us-east-2"}))

(defn download-document
  "Returns a stream"
  [document-key]
  (-> (aws/invoke s3
                  {:op :GetObject :request {:Bucket bucket
                                            :Key document-key}})
      (:Body)))

(comment (download-document "10/metadata.xml"))

(defn upload-object
  [key serialized-object]
  [:pre [(and (string? serialized-object) (not-empty serialized-object))]]
  (aws/invoke s3 {:op :PutObject
                  :request {:Bucket bucket
                            :Key key
                            :Body (io/input-stream (.getBytes serialized-object))}}))

(defn get-page-objects
  ([] (aws/invoke s3 {:op :ListObjectsV2 :request {:Bucket bucket}}))
  ([cont-token]
   (aws/invoke s3 {:op :ListObjectsV2 :request {:Bucket bucket
                                                :ContinuationToken cont-token}})))

(defn get-keys-from-page
  [page]
  (->> (:Contents page)
       (map :Key)))

(defn get-metadata-files-from-page
  [page]
  (->> (get-keys-from-page page)
       (filter #(str/ends-with? %  "metadata.xml"))))

(defn get-page
  ([]
   (let [objs (get-page-objects)
         metadata-files (get-metadata-files-from-page objs)
         cont-key (:NextContinuationToken objs)]
     {:cont-token cont-key :files metadata-files}))
  ([cont-token] (let [objs (get-page-objects cont-token)
                      pdfs (get-metadata-files-from-page objs)
                      cont-key (:NextContinuationToken objs)]
                  (μ/log ::fetched-page :page-count (count pdfs))
                  {:cont-token cont-key :files pdfs})))

(defn process-files [c files]
  (run! #(>!! c %) files))

(defn download-all!
  [c]
  (let [{:keys [cont-token files]} (get-page)]
    (process-files c files)
    (loop [t cont-token
           cnt (count files)
           page-number 1]

      (if t
        (let [{:keys [cont-token files]}
              (get-page t)]
          (process-files c files)
          (recur cont-token (+ cnt (count files)) (inc page-number)))
        cnt))))
