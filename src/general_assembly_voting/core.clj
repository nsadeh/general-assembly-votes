(ns general-assembly-voting.core
  (:gen-class)
  (:require [clojure.core.async :refer [<!! >!! chan thread]]
            [com.brunobonacci.mulog :as µ]
            [com.brunobonacci.mulog :as μ]
            [general-assembly-voting.file-storage :as storage]
            [general-assembly-voting.process :as process]))

(defn process
  []
  (let [c (chan)
        status (thread
                 (do (try {:status :succeeded
                           :count (storage/download-all! c)}
                          (catch Throwable t
                            (μ/log ::download-error
                                   :error (Throwable->map t))))
                     (>!! c :complete)))
        process (fn []
                  (thread
                    (loop [document-key (<!! c)]
                      (if (= :complete document-key)
                        (do
                          (μ/log ::work-complete)
                          :complete)
                        (do (try
                              (process/process-document document-key)
                              (catch Exception e
                                (µ/log ::error-processing-document
                                       :key document-key
                                       :message (ex-message e)
                                       :data (ex-data e))))
                            (recur (<!! c)))))))]
    (dorun 20 (repeatedly process))
    status))

(μ/set-global-context! {:app-name "general-assembly-voting-records"
                        :version "1.0"})

(μ/start-publisher! {:type :multi
                     :publishers [{:type :console-json
                                   :pretty? true
                                   :transform identity}
                                  {:type :simple-file
                                   :filename "logs.log"
                                   :transform identity}]})

(defn -main
  "I don't do a whole lot ... yet."
  []
  (process))

(comment (-main))