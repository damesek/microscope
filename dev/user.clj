(ns user
  (:require [microscope.monitor :refer [defn<> extract<> let<>
                                        current-function-name<> line<>
                                        filename<> methodname<> full<>
                                        tryfn<>]]))

(comment
  (defn<> he
          [x y]
          (+ x y))


  (let<>
    [x 1 y 2]
    (+ x y))

  (defn debug-test2 [i]
    (for [x (into [] (range i))]
      (do (extract<>) x)))

  (coll? (for [x (into [] (range 5))]
          (do (extract<>) x)))

  (defn debug-test2 [i]
    (pr (current-function-name<>)
        (line<>)
        (filename<>)
        (full<>)
        )
    (for [x (into [] (range i))]
      (do x)))

  (debug-test2 2)

  (defn<> foo [a b & [c]]
    (if c
      (* a b c)
      (* a b 100)))

  (map #(do % (extract<>)) (list* [a b]))
  (walk (partial postwalk-code foo) foo [2 3])





(defn fooo [a b & [c]]

  (if c
    (do (extract<>) (* a b c))
    (do (extract<>) (* a b 100))))

(defmacro info-about-caller []

  (pprint {:form &form :env &env}) `(println "macro was called!"))

  (let<> [i 3]
          (for [(do (print x) x) (into [] (range i))]
            x))


(let [i 3]
       (for [~`(do (print x) x) (into [] (range i))]
         x))

  (let<> [a (take 5 (range))
        {:keys [b c d] :or {d 10 b 20 c 30}} {:c 50 :d 100}
        [e f g & h] ["a" "b" "c" "d" "e"]]
    [a b c d e f g h])


  (tryfn<> he [x y] (+ x y))

  (he 2 3)
  (he 2 "a")

  ;=>
  ;Caught exception:
  ;java.lang.String cannot be cast to java.lang.Number
  ;Caught environment:  :in-ns  user
  ;:current-function user$he
  ;:where 67:3
  ;:fn-arglist ([x y])
  ;:got-args {x 2, y a}
  ;:function-name he
  ;:elapsed-time Elapsed time: 1.076506 ms

  (try-> (str " Get single range call\n form ns: " *ns* "\n")
         (add-two 2 "a"))

  )
