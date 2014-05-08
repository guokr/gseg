(ns gseg.app
  (:use [ring.adapter.jetty]
        [com.guokr.nlp.seg]
        [ring.middleware params keyword-params nested-params])
  (:require [com.climate.claypoole :as cp]
            [clojure.tools.logging :as logging]
            [clj-pid.core :as pid])
  (:gen-class :main true))

(org.apache.log4j.MDC/put
  "PID"
  (.getName (java.lang.management.ManagementFactory/getRuntimeMXBean)))

(def logger (org.slf4j.LoggerFactory/getLogger "gseg"))

(defn- load-config []
  (try
    (let [yaml (org.yaml.snakeyaml.Yaml.)]
      (.load yaml (java.io.FileReader. "config/gseg.yaml")))
    (catch java.io.IOException e
      (.warn logger "config file gseg.yaml was not found, loading the default config"))))

(defn init-seg-pool [pool-size] (cp/threadpool pool-size :daemon true))

(defn init-mon-queue [queue-size]
  (java.util.concurrent.LinkedBlockingQueue. queue-size))

(defn monitor [mon-queue]
  (fn []
    (while true (let [[appkey invkey seged] (.take mon-queue)]
                  ;(logger/info appkey invkey seged)
                  nil
                  ))))

(defn gseg [seg-pool mon-queue request]
  (let [appkey (get-in request [:params :appkey])
        invkey (get-in request [:params :invkey])]
    (if (nil? appkey)
      {:status 401 :headers {"Content-Type" "text/plain; charset=utf-8"}
       :body "appkey can not be empty"}
      (let [seged (with-open [rdr (clojure.java.io/reader (:body request))]
                    (doall (cp/pmap seg-pool seg (line-seq rdr))))]
        (.put mon-queue [appkey invkey seged])
        {:status 201 :headers {"Content-Type" "text/plain; charset=utf-8"}
         :body (clojure.string/join "\n" seged)}))))

(def app (-> gseg
             wrap-keyword-params
             wrap-params))

(defn -main [& args]
  (let [config (merge {"host" "0.0.0.0" "port" 3333 "pidfile" "log/pid"
                       "poolsize" 8 "queuesize" 10000} (load-config))
        {pid-file "pidfile" host "host" port "port"
         pool-size "poolsize" queue-size "queuesize"} config
        seg-pool (init-seg-pool pool-size)
        mon-queue (init-mon-queue queue-size)
        app (-> (partial gseg seg-pool mon-queue)
                wrap-keyword-params
                wrap-params)]
    (pid/save pid-file)
    (pid/delete-on-shutdown! pid-file)

    (try
      (seg "分词测试")
      (.start (Thread. (monitor mon-queue)))
      (run-jetty app {:host host :port port})
      (catch java.lang.Throwable e
        (.error logger "Server Error!" e)
        (System/exit -1)))))
