(defproject general-assembly-voting "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [environ "1.2.0"]
                 [clj-http "3.12.3"]
                 [com.cognitect.aws/api "0.8.686"]
                 [com.cognitect.aws/endpoints "1.1.12.504"]
                 [com.cognitect.aws/s3 "848.2.1413.0"]
                 [io.pinecone/pinecone-client "0.7.2"]
                 [com.taoensso/carmine  "3.3.2"]
                 [org.clojure/data.xml "0.2.0-alpha8"]
                 [cheshire "5.12.0"]
                 [com.brunobonacci/mulog-adv-console "0.9.0"]
                 [net.clojars.wkok/openai-clojure "0.16.0"]
                 [com.knuddels/jtokkit "1.0.0"]
                 [org.clj-commons/hickory "0.7.3"]]
  :main ^:skip-aot general-assembly-voting.core
  :plugins [[lein-environ "1.2.0"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
