(ns user
  (:require [microscope.monitor :refer [defn<> extract<> let<>
                                        current-function-name<> line<>
                                        filename<> methodname<> full<>
                                        unmangle]]))

(comment
  (defn<> he
          [x y]
          (let [valami [x y]]
            (+ x y)))

  (let<>
    [x 1 y 2]
    (+ x y))

  (defn debug-test2 [i]
    (for [x (into [] (range i))]
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

  )


