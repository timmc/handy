(ns org.timmc.handy.t-repl
  (:require [org.timmc.handy.repl :as r])
  (:use clojure.test))

(deftest formatting
  (are [c fq? s] (binding [r/*show-fq* fq?]
                   (= (r/format-classname c) s))
       String false "String"
       String true "java.lang.String"
       Integer/TYPE false "int"
       Integer/TYPE true "int"
       (class (int-array [])) false "int[]"
       (class (int-array [])) true "int[]"
       (class (into-array [(into-array Void [])])) false "Void[][]"
       (class (into-array [(into-array Void [])])) true "java.lang.Void[][]"))
