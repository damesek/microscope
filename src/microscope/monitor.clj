(ns microscope.monitor
  (:require [clojure.pprint :as pprint])
  (:import (java.util.regex Pattern)))

(def current-function (atom nil))

; not work as I planned..
(defmacro spy-form []
  "line, caller name"
  `(binding [*print-meta* true]
              (pr '~&form)))


; Thanks to Andy Fingerhut/ Clojurians Slack
; re-sub from deprecated clojure.contrib.str-utils/re-sub

(defn- re-sub
  "Replaces the first instance of 'pattern' in 'string' with
  'replacement'.  Like Ruby's 'String#sub'.

  If (ifn? replacement) is true, the replacement is called with
  the match.
  "
  [^Pattern regex replacement ^String string]
  (if (ifn? replacement)
    (let [m (re-matcher regex string)]
      (if (.find m)
        (str (.subSequence string 0 (.start m))
             (replacement (re-groups m))
             (.subSequence string (.end m) (.length string)))
        string))
    (.. regex (matcher string) (replaceFirst replacement))))

(defn unmangle
  "Given the name of a class that implements a Clojure function,
   returns the function's name in Clojure"
  [class-name]
  (.replace (re-sub #"^(.+)\$(.+)__\d+$" "$1/$2" class-name) \_ \-))


(defmacro current-function-name<> []
  "Returns a string, the name of the current Clojure function"
  `(-> (Throwable.) .getStackTrace first .getClassName ))

(defmacro line<> []
  `(-> (Throwable.) .getStackTrace first .getLineNumber))

(defmacro filename<> []
  `(-> (Throwable.) .getStackTrace first .getFileName))


(defmacro methodname<> []
  `(-> (Throwable.) .getStackTrace first .getMethodName))

(defmacro full<> []
  `(-> (Throwable.) .getStackTrace first))

(defmacro current-fn [& args]
    "Returns a string, the name of the current Clojure function"
    `(map #(-> (Throwable.) .getStackTrace first `%) ~@args))


(defmacro extract<>
  "Get back all values, temp params too
   (defn valami []
     (for [x (into [] (range 5))]
      (do  (m/debug)
         (println x))))"
  []
  (reset! current-function (current-function-name<>))
    `(let [start# (System/nanoTime)]
       (clojure.pprint/pprint
         (hash-map ~@(->> &env keys (mapcat (fn [x] [`'~x x])))))
       ; (println (binding [*print-meta* true] (pr "form data " '~&form)))
       (println " current ns" (str *ns*)
                ;"\n which function " (deref current-function)
                "\n current function " (current-function-name<>)
                "\n function meta" (meta (ns-resolve *ns* (symbol ((clojure.string/split (current-function-name<>) #"\$") 1)))))
       ;(current-fn .getLineNumber .getFileName.getMethodName))
       ;(println (meta (intern (symbol (str *ns*)) (second ,,,fixit,,,))))
       (println "Elapsed time: " (/ (- (System/nanoTime) start#) 1000000.0) " ms")))

(defmacro defn<> [fn bindings & body]
  `(do (defn ~fn ~bindings (extract<>) ~@body)))


;; From  https://github.com/scottjad/uteal

(defmacro let<>
  "let value inspected, no temporary values
   use eg. (dlet [x 3 y 5] x)"
  [bindings & body]
  `(let [~@(mapcat (fn [[n v]]
                     (if (or (vector? n) (map? n))
                       [n v]
                       [n v '_ `(println (current-function-name<>) (name '~n) ":" ~v)]))
                   (partition 2 bindings))]
     ~@body))