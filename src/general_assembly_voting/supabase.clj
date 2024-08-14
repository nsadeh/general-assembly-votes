(ns general-assembly-voting.supabase
   (:require [general-assembly-voting.env :refer [supabase]]
             [cheshire.core :as cheshire]
             [clj-http.client :as http]))

(def url
  (str "https://" (:slug supabase) ".supabase.co/rest/v1"))

(def messages-url
  (str url "/unga_voting_records"))

(def headers
  {"apikey" (:access-token supabase)
   "Authorization" (str "Bearer " (:access-token supabase))
   "Content-Type" "application/json"})

(def options
  {:headers headers
   :coerce :always
   :as :json})

(defn insert-records
  [records]
  (->> (cheshire/generate-string records)
       (assoc options :body)
       (http/post messages-url)))