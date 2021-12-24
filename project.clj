(defproject org.timmc/handy "1.7.2-SNAPSHOT"
  :description "Common utilities to fill in the gaps"
  :url "https://github.com/timmc/handy"
  :license {:name "Eclipse Public License - v1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  ;; lein 1.x repl throws with :repl-options :init-ns, so use :main instead
  :main ^:skip-aot org.timmc.handy
  ;; lein 2.x repl throws on Clojure 1.2.0, so use 1.3.0 instead
  :dependencies [[org.clojure/clojure "1.3.0"]]
  ;; All versions of Clojure that we want to test against (including
  ;; default dependency.)
  :profiles {:1.2.0 {:dependencies [[org.clojure/clojure "1.2.0"]]}
             :1.2.1 {:dependencies [[org.clojure/clojure "1.2.1"]]}
             :1.3.0 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4.0 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5.0 {:dependencies [[org.clojure/clojure "1.5.0"]]}
             :1.5.1 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.6.0 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7.0 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8.0 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.9.0 {:dependencies [[org.clojure/clojure "1.9.0"]]}
             :1.10.3 {:dependencies [[org.clojure/clojure "1.10.3"]]}
             :dev {:plugins [[lein-release "1.0.5"]]}}
  :aliases {"all-clj" ["with-profile" "+1.2.0:+1.2.1:+1.3.0:+1.4.0:+1.5.0:+1.5.1:+1.6.0:+1.7.0:+1.8.0:+1.9.0:+1.10.3"]}
  :global-vars {*warn-on-reflection* true}
  :lein-release {:scm :git
                 :deploy-via :shell
                 :shell ["lein" "deploy" "clojars"]})
