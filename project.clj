(defproject hu.baader/microscope "0.1.0-SNAPSHOT"
  :description "My information extractor about what happens in functions"
  :url "https://github.com/damesek/microscope"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :plugins [[lein-codox "0.10.7"]]
  :codox {:source-paths ["src/"]
          :metadata {:doc/format :markdown}
          :output-path "codox"}
  :deploy-repositories {"releases" {:url "https://repo.clojars.org" :sign-releases false}}
  :repl-options {:init-ns user})
