(ns general-assembly-voting.parse-metadata
  (:require [clojure.data.xml :as xml]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [com.brunobonacci.mulog :as µ]
            [general-assembly-voting.file-storage :as storage]))

(defn load-metadata
  [metadata-stream]
  (try (-> (io/input-stream metadata-stream)
           (xml/parse))
       (catch Exception e
         (µ/log ::error-parsing-metadata
                :message (ex-message e)
                :data (ex-data e)))))

(defn- get-datafields
  [field-tag xml-data]
  (some->>  xml-data :content
            (filter #(= field-tag (get-in % [:attrs :tag])))))
(defn- get-datafield
  [field-tag xml-data]
  (-> (get-datafields field-tag xml-data)
      (first)))

(defn- get-subfield-data
  [subfield-tag datafield]
  (some->> datafield :content
           (filter map?)
           (filter #(= subfield-tag (get-in % [:attrs :code])))
           (first)
           (:content)
           (first)))

(defn get-tagged-content
  [field-tag subfield-tag xml-data]
  (->> (get-datafield field-tag xml-data)
       (get-subfield-data subfield-tag)))

(defn get-tagged-contents
  [field-tag subfield-tag xml-data]
  (->> (get-datafields field-tag xml-data)
       (map (partial get-subfield-data subfield-tag))))

(defn extract-date-string [xml-data]
  (-> (get-tagged-content "269" "a" xml-data)
      (str/trim)
      (not-empty)
      (or "undated")))

(defn voting-record?
  [xml-data]
  (some-> (get-tagged-content "089" "a" xml-data)
          (str/trim)
          (str/lower-case)
          (= "voting record")))

(defn vote
  "Transforms the vote string into a vote value"
  [vote-str]
  (case (some->> vote-str
                 (str/lower-case)
                 (str/trim))
    "y" :for
    "n" :against
    "a" :abstain
    :not-voted))

(defn format-resolution
  [xml-data]
  (let [date (extract-date-string xml-data)
        document-id (first (:content (get-datafield "001" xml-data)))
        title (get-tagged-content "245" "a" xml-data)
        status (get-tagged-content "245" "c" xml-data)
        records (get-datafields "967" xml-data)
        make-record (fn [record]
                      (hash-map :nation (get-subfield-data "e" record)
                                :vote (vote (get-subfield-data "d" record))
                                :date date
                                :title title
                                :status status
                                :document_id document-id))]
    (map make-record records)))

(comment (vote nil))

(comment (->> (storage/download-document "10/metadata.xml")
              (load-metadata)
              (get-tagged-content "269" "a")))

(comment (let [document-id "4048673"
               document (storage/download-document (str document-id "/metadata.xml"))
               parsed (load-metadata document)]
           (format-resolution  parsed)))

(comment (let [key "1009754/metadata.xml"
               document (storage/download-document key)
               parsed (load-metadata document)]
           (voting-record? parsed)))
