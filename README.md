# microscope

This is a **microscope** for Clojure functions.<BR>
[I created a few years ago for myself to debug things.]

`[hu.baader/microscope "0.1.0-SNAPSHOT"]`


Few things:
- UNDER DEVELOPMENT - do not use
- defn<> bug plus missing meta etc handling, I created for quick fn tests
- my first test repo, mainly for deployment test (use carefully)

Todo: 
 - missing tests
 - codox


I didn't test in production environment.<BR> 
Explore, extract extract information.


## Usage

Very easy to use:

```clojure
(ns your.ns
  (:require [microscope.monitor :refer [defn<> extract<> let<>
                                        current-function-name<> line<>
                                        filename<> methodname<> full<>]]))
```

Use defn<> microscope function instead of clojure.core defn:
 ```Clojure
  (defn<> he [x y]
           (let [valami [x y]]
            (+ x y)))
```
You will get back from `(ns user)`:

```clojure
{x 2, y 3}
 current ns user 
 current function  user$he
Elapsed time:  2.364  ms
=> 5
```


Use let<> binding:

```clojure
 (let<>
   [x 1 y 2]
   (+ x y))
```

You will get back:

```clojure
user$eval2835 x : 1
user$eval2835 y : 2
=> 3
```

Use extract<> for extract informations from loops, specific places. 

```clojure
  (defn test2 [i]
    (for [x (into [] (range i))]
      (do (extract<>) x)))
```

You will get back:

```clojure
(test2 3)
{x 0,
 temp__5720__auto__ (0 1 2),
 i 3,
 b__2842
 #object[clojure.lang.ChunkBuffer 0x5097956b "clojure.lang.ChunkBuffer@5097956b"],
 i__2841 0,
 c__6288__auto__
 #object[clojure.lang.ArrayChunk 0x5465c307 "clojure.lang.ArrayChunk@5465c307"],
 iter__2839
 #object[user$test2$iter__2839__2843 0x2f1e5e7 "user$test2$iter__2839__2843@2f1e5e7"],
 size__6289__auto__ 3,
 s__2840 (0 1 2)}
 current ns user 
 current function  user$test2$iter__2839__2843$fn__2844$fn__2845
Elapsed time:  30.849502  ms
{x 1,
 temp__5720__auto__ (0 1 2),
 i 3,
 b__2842
 #object[clojure.lang.ChunkBuffer 0x5097956b "clojure.lang.ChunkBuffer@5097956b"],
 i__2841 1,
 c__6288__auto__
 #object[clojure.lang.ArrayChunk 0x5465c307 "clojure.lang.ArrayChunk@5465c307"],
 iter__2839
 #object[user$test2$iter__2839__2843 0x2f1e5e7 "user$test2$iter__2839__2843@2f1e5e7"],
 size__6289__auto__ 3,
 s__2840 (0 1 2)}
 current ns user 
 current function  user$test2$iter__2839__2843$fn__2844$fn__2845
Elapsed time:  12.692063  ms
{x 2,
 temp__5720__auto__ (0 1 2),
 i 3,
 b__2842
 #object[clojure.lang.ChunkBuffer 0x5097956b "clojure.lang.ChunkBuffer@5097956b"],
 i__2841 2,
 c__6288__auto__
 #object[clojure.lang.ArrayChunk 0x5465c307 "clojure.lang.ArrayChunk@5465c307"],
 iter__2839
 #object[user$test2$iter__2839__2843 0x2f1e5e7 "user$test2$iter__2839__2843@2f1e5e7"],
 size__6289__auto__ 3,
 s__2840 (0 1 2)}
 current ns user 
 current function  user$test2$iter__2839__2843$fn__2844$fn__2845
Elapsed time:  8.50809  ms
=> (0 1 2)
```

Use other single-line functions as (I use for error messages sometimes): 
- `full<>`
- `current-function-name<>`
- `line<>`
- `filename<>`

For example:

```clojure
(defn debug-test2 [i]
    (pr (current-function-name<>)
        (line<>)
        (filename<>)
        (full<>))
    (for [x (into [] (range i))]
      x))
```

 Run `(debug-test2 2)` and you will get:
 
 ```clojure
"user$debug_test2" 3 "form-init2180815951943850242.clj" 
[user$debug_test2 invokeStatic "form-init2180815951943850242.clj" 5]
=> (0 1)
```


## License

Copyright © 2021 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
