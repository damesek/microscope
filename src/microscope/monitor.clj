(ns microscope.monitor
  (:require [clojure.pprint :as pprint]
            [clojure.string :as str])
  (:use [clojure.walk :only [postwalk]])
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
  `(-> (Throwable.) .getStackTrace first .getClassName))

(defmacro line<> []
  `(-> (Throwable.) .getStackTrace first .getLineNumber))

(defmacro filename<> []
  `(-> (Throwable.) .getStackTrace first .getFileName))


(defmacro methodname<> []
  `(-> (Throwable.) .getStackTrace first .getMethodName))

(defmacro full<> []
  `(-> (Throwable.) .getStackTrace first))

(defmacro current-fn<> [& args]
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
     ;(current-fn .getLineNumber .getFileName .getMethodName))
     ;(println (meta (intern (symbol (str *ns*)) (second ,,,fixit,,,))))
     (println "Elapsed time: " (/ (- (System/nanoTime) start#) 1000000.0) " ms")))

(def fn-state (atom nil))

(defmacro look<>
  "same as extract<>, give back data instead of print"
  []
  (reset! current-function (current-function-name<>))
  `(let [start# (System/nanoTime)]
     ; (println (binding [*print-meta* true] (pr "form data " '~&form)))
     (reset! fn-state {:ns               (str *ns*)
                       :current-function (current-function-name<>)
                       :function-meta    (meta (ns-resolve *ns* (symbol ((clojure.string/split (current-function-name<>) #"\$") 1))))
                       ;:current-fn-test (current-fn .getLineNumber .getFileName.getMethodName)
                       :got-args         (hash-map ~@(->> &env keys (mapcat (fn [x] [`'~x x]))))
                       :elapsed-time     (str "Elapsed time: " (/ (- (System/nanoTime) start#) 1000000.0) " ms")})
     ))


(defn- print-fn [expr]
  (cond (list? expr) (if-not (str/includes? (str (first expr)) "microscope")
                       expr
                       (apply concat (rest expr)))
        :else expr))

(def- print-line (atom -1))


(defmacro- print-expr-results [expr]
  `(let [result# ~expr
         lk# (meta (ns-resolve *ns* (symbol ((str/split (current-function-name<>) #"\$") 1))))
         helper# (partial print-fn)
         form# (postwalk helper# '~expr)
         result# (postwalk helper# result#)]
     ~`(swap! print-line inc)
     (println @print-line form# "=>" result#)
     result#))

; from https://gist.github.com/duckyuck debug
(defn- over-expr-debug [form]
  (if (list? form)               ;; How can we detect quoted form?
    `(print-expr-results ~form)
    form))


(defmacro debug<> [form]
  "Prints the form (and all nested forms) and their result to *stdout*."
  (postwalk over-expr-debug form))


(defmacro defn<> [f args & all]
  "Works like `defn`, but when something inside it throws, you get to see which form threw."
  (let [form# `(defn ~f ~args (do (reset! print-line -1)
                                  (let [cf# (current-function-name<>)
                                        lk# (meta (ns-resolve *ns* (symbol ((clojure.string/split cf# #"\$") 1))))]
                                    (println
                                      "\n :function name " cf#
                                      "\n :where         " (str (->> lk# :line) ":" (->> lk# :column))
                                      "\n :fn-arglist    " (->> lk# :arglists)
                                      "\n :got-args      " (hash-map ~@(->> &env keys (mapcat (fn [x] [`'~x x]))))
                                      "\n -------------------------------- \n "
                                      )))  ~@all)]
    ;(str/replace ~@all #"'([{(#\[])" "(quote "))]

    (postwalk
      (fn [expr] (if (list? expr)                           ;; How can we detect quoted form?
                   `(print-expr-results ~expr)
                   expr)) form#)))


(defmacro tryfn<> [fn bindings & body]
  `(do (defn ~fn ~bindings
         (let [lk# ~`(look<>)]
           (try ~@body
                (catch Exception e#
                  (println "Caught exception:\n" (.getMessage e#) "\n"
                           "\nCaught environment:\n"
                           ":in-ns " (:ns lk#)
                           "\n :current-function" (:current-function lk#)
                           "\n :where" (str (->> lk# :function-meta :line) ":" (->> lk# :function-meta :column))
                           "\n :fn-arglist" (->> lk# :function-meta :arglists)
                           "\n :got-args" (->> lk# :got-args)
                           "\n :function-name" (->> lk# :function-meta :name)
                           "\n :elapsed-time" (->> lk# :elapsed-time))))))))



;; From  https://github.com/scottjad/uteal

(defmacro let<>
  "let value inspected, no temporary values
   use eg. (dlet [x 3 y 5] x)"
  [bindings & body]
  `(let [~@(mapcat (fn [[n v]]
                     (if (or (vector? n) (map? n))
                       [n v]
                       [n v
                        '_ `(println                        ;(current-function-name<>)
                              (name '~n) ":" ~v)]))
                   (partition 2 bindings))]
     ~@body))

(comment                                                    ; different approach

  (defmacro let
    "binding => binding-form init-expr

    Evaluates the exprs in a lexical context in which the symbols in
    the binding-forms are bound to their respective init-exprs or parts
    therein."
    {:added "1.0", :special-form true, :forms '[(let [bindings*] exprs*)]}
    [bindings & body]
    (assert-args
      (vector? bindings) "a vector for its binding"
      (even? (count bindings)) "an even number of forms in binding vector")
    `(let* ~(destructure bindings) ~@body))

  (debug<> (+ (+ 1 2 (+ 10 20 (+ 30 40))) (+ 3 4)))

  (defn<> deb [a b]
          (for [x (into [] (range a))]
            (* 3 (+ 2 b))))

  (deb 2 3)


  (defn ^{:private true}
    maybe-destructured
    [params body]
    (if (every? symbol? params)
      (cons params body)
      (loop [params params
             new-params (with-meta [] (meta params))
             lets []]
        (if params
          (if (symbol? (first params))
            (recur (next params) (conj new-params (first params)) lets)
            (let [gparam (gensym "p__")]
              (recur (next params) (conj new-params gparam)
                     (-> lets (conj (first params)) (conj gparam)))))
          `(~new-params
             (let ~lets
               ~@body))))))


  (defmacro dbg-let
    [[_ bindings & subforms :as form] locals opts]
    `(dbg-base ~form ~locals ~opts
               (let ~(->> (partition 2 bindings)
                          (mapcat (fn [[sym value :as binding]]
                                    [sym value
                                     '_ `(ut/spy-first ~(if (coll? sym)
                                                          (ut/replace-& sym)
                                                          sym)
                                                       '~sym
                                                       ~opts)]))
                          vec)
                 ~@subforms)))

  )
