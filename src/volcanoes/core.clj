(ns volcanoes.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]))


;; main ways to execute code in your files

;; 1. whole file           : , s b
;; 2. Top-level form       : , e f
;; 3. Single expression    : , e e

;; 4. REPL prompt

;; Guideline
;; (not so important...)
;; compile the thing you are working on : namespace, function or expression

;; Ergonomics

;; 1. Autocomplete
;;    unintrusive <-> intrusive
;; 2. Parenthesis matching
;; 3. Visual Prompts

(def csv-lines
  (with-open [csv (io/reader "GVP_Volcano_List_Holocene.csv")]
    (doall
     (csv/read-csv csv))))

(defn transform-header [header]
  (if (= "Elevation (m)" header)
    :elevation-meters
    (-> header
        clojure.string/lower-case
        (clojure.string/replace #" " "-")
        keyword)))

(defn transform-header-row [header-line]
  (map transform-header header-line))

(def volcano-records
  (let [csv-lines (rest csv-lines)
        header-line (transform-header-row(first csv-lines))
        volcano-lines (rest csv-lines)]
    (map (fn [volcano-line]
           (zipmap header-line volcano-line))
         volcano-lines)))


(defn slash->set [s]
  (set (map str/trim (str/split s #"/"))))

(defn parse-eruption-date [date]
  (if (= "Unknown" date)
    nil
    (let [[y e] (clojure.string/split date #" ")]
      (cond
        (= e "BCE") (- (Integer/parseInt y))
        (= e "CE") (Integer/parseInt y)
        :else (throw (ex-info "Could not parse year." {:year date}))))))

(defn parse-attributes [volcano]
  (-> volcano
      (update :elevation-meters #(Integer/parseInt %))
      (update :longitude #(Double/parseDouble %))
      (update :latitude #(Double/parseDouble %))
      (update :last-known-eruption parse-eruption-date)
      (update :tectonic-setting slash->set)
      (update :dominant-rock-type slash->set)
      ))

(def volcanoes-parsed
  (mapv parse-attributes volcano-records))


(def types (set (map :primary-volcano-type volcano-records)))

;; Comment to disable a form:

#_(println "All done.")

(comment

  ;; REPL-driven code

  (let [volcano (nth volcanoes-parsed 100)]
    (clojure.pprint/pprint volcano))

  (let [volcano (first (filter #(= "221291" (:volcano-number %)) volcanoes-parsed))]
    (clojure.pprint/pprint volcano))

  )

;; run-length-encoding
;; [:a :a :a :b]
;; [[3 :a] [1 :b]]

(defn rle [l]
  (map (juxt count first) (partition-by identity l)))

;; drop every nth element
(defn drop-every [n l]
  (mapcat #(take (dec n) %) (partition-all n l)))
