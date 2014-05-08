(defproject gseg "0.0.1"

  :description "A clojure server for Stanford CoreNLP seg"
  :url "https://github.com/guokr/gseg/"

  :license {:name "Eclipse Public License - v 1.0"
          :url "http://www.eclipse.org/legal/epl-v10.html"
          :distribution :repo
          :comments "same as Clojure"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.slf4j/slf4j-api "1.7.7"]
                 [org.slf4j/slf4j-log4j12 "1.7.7"]
                 [org.yaml/snakeyaml "1.13"]
                 [com.climate/claypoole "0.2.1"]
                 [com.guokr/clj-cn-nlp "0.2.1"]
                 [clj-pid "0.1.1"]
                 [ring/ring-jetty-adapter "1.2.0"]]

  :source-paths ["src"]
  :uberjar-name "gseg-standalone.jar"
  :main gseg.app

  :jvm-opts ["-Xmx4g" "-server"])

