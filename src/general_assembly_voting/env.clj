(ns general-assembly-voting.env
  (:require [environ.core :refer [env]]))

(def digital-ocean {:access-key (or (env :digital-ocean-access-key) "")
                    :secret-key (or (env :digital-ocean-secret-key) "")
                    :hostname (env :digital-ocean-spaces-host)
                    :bucket (env :digital-ocean-spaces-bucket)})

(def openai-token (env :openai-key))

(comment openai-token)

(def supabase {:access-token (env :supabase-access-token)
               :slug (env :supabase-slug)})
