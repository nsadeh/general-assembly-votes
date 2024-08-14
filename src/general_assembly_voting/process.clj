(ns general-assembly-voting.process
  (:require [com.brunobonacci.mulog :as µ]
            [general-assembly-voting.file-storage :as storage]
            [general-assembly-voting.parse-metadata :as metadata]
            [general-assembly-voting.supabase :as supabase]))

(defn process-document
  "Process a document by downloading it, parsing it, and storing the results in Supabase"
  [document-key]
  (µ/with-context {:document-key document-key}
    (µ/log ::downloading-key)
    (let [document (storage/download-document document-key)
          _ (µ/log ::key-downloaded)
          parsed (metadata/load-metadata document)]
      (if (metadata/voting-record? parsed)
        (do (µ/log ::processing-voting-record)
            (try
              (->> parsed
                   (metadata/format-resolution)
                   (supabase/insert-records))
              (catch Exception e
                (µ/log ::error-inserting-records
                       :message (ex-message e)
                       :data (ex-data e)))))
        (µ/log ::not-voting-record)))))

(comment (process-document "4048673/metadata.xml"))
