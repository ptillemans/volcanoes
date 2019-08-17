(ns volcanoes.core-test
  (:require [clojure.test :refer :all]
            [volcanoes.core :refer :all]))

(deftest rle-test
  (testing "RLE "
    (is (= (rle [:a :a :a :b :b :d :a :a :a]) [[3 :a] [2 :b] [1 :d] [3 :a]]))))
