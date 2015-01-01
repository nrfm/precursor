(ns pc.utils
  (:require clojure.pprint
            [clojure.tools.logging :as log]
            [schejulure.core :as schejulure]
            [slingshot.slingshot :refer (try+)]))

(defmacro inspect
    "prints the expression '<name> is <value>', and returns the value"
    [value]
    `(do
       (let [value# (quote ~value)
             result# ~value]
         (when (= java.io.PrintWriter (type *out*))
           (println value# "is" (with-out-str (clojure.pprint/pprint result#))))
         (log/infof "%s is %s" value# result#)
         result#)))

(defmacro connect-browser-weasel []
  `(do
     (require 'weasel.repl.websocket)
     (cemerick.piggieback/cljs-repl :repl-env (weasel.repl.websocket/repl-env :ip "0.0.0.0" :port 9001))))

(defn remove-map-nils [unnested-map]
  (into {} (remove (comp nil? last) unnested-map)))

(defn update-when-in
  "update-in, but only if the nested sequence of keys already exists!"
  [m ks f & args]
  (let [sentinel (Object.)]
    (if-not (identical? sentinel (get-in m ks sentinel))
      (apply update-in m ks f args)
      m)))

(defmacro straight-jacket*
  [& body]
  `(do
     (try
       (try+
        (try+
         (do
           ~@body)
         (catch Object e#
           (log/error e# "1st straight jacket")
           ;; need rollbar integration
           ;; (rollbar/rollbar :exception (-> ~'&throw-context :throwable)
           ;;                    :data {:cmd (str (quote ~body))})
           ))
        (catch Object _#
          (let [t# (-> ~'&throw-context :throwable)]
            (.printStackTrace ^Throwable t#)
            (println "2")
            (log/errorf t# "straight-jacket"))))
       (catch Exception e#
         (println "3")
         (println "*** Straight Jacket WTF ***")))
     nil))

(defmacro straight-jacket
  "For sections of code that are not allowed to fail. All exceptions
  will be caught, rollbar will be attempted. If rollbar fails, that
  exception will be caught, and a message logged. If that fails, just
  give up and cry about it."
  [& body]
  `(straight-jacket* ~@body))

(defonce safe-scheduled-jobs (ref {}))

(defn safe-schedule
  "Schedules var to be run on schedule. Will cancel old schedule if called multiple times
  with the same var."
  [schedule f]
  (assert (var? f))
  (let [new-job (schejulure/schedule schedule (fn [] (future (f))))]
    (dosync
     (alter safe-scheduled-jobs
            (fn [jobs]
              (let [job-name (str f)
                    old-job (get jobs job-name)]
                (when old-job (.cancel old-job false))
                (assoc jobs job-name new-job)))))))
