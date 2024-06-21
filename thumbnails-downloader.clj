#!/usr/bin/env bb

(require '[babashka.fs :as fs])
(require '[babashka.http-client :as http])
(require '[clojure.java.io :as io])  ;; aliased automatically but needed for lsp
(require '[clojure.string :as str])  ;; aliased automatically but needed for lsp

(defn get-thumbnail! [game-name path]
  (let [encoded-game-name (-> game-name
                              (str/replace " " "%20")
                              (str/replace "[" "%5B")
                              (str/replace "]" "%5D")
                              (str/replace "&" "_"))
        game-filename-img (str path "/" game-name ".png")]
    (try
      (if (fs/exists? game-filename-img)
        (println game-filename-img "exists, skipping.")
        (do
          (println "Downloading" game-filename-img "...")
          (io/copy
           (:body (http/get (str "https://thumbnails.libretro.com/Nintendo%20-%20Super%20Nintendo%20Entertainment%20System/"
                                 path
                                 "/"
                                 encoded-game-name
                                 ".png")
                            {:as :stream}))
           (io/file game-filename-img))))
      (catch Exception e
        (println (str "[" game-name "] Caught exception: " (.getMessage e) "\n"))))))

(defn name-wo-ext [file]
  (let [filename (fs/file-name file)
        position (str/last-index-of filename ".")]
    (.substring filename 0 position)))

(let [games (map str (fs/glob "/Users/dan/Library/Mobile Documents/com~apple~CloudDocs/roms/nes/" "*.nes"))
      filenames (map name-wo-ext games)]
  (println (count filenames) "games found!")
  (run! (fn [filename]
          ;; using map instead of run! will not execute the get-thumbnail! function
          (run! (partial get-thumbnail! filename) ["Named_Boxarts" "Named_Snaps" "Named_Titles"]))
        filenames))
(comment
  (run! (fn [person] (map (partial println person) ["Ernesto" "Daniel" "Virginia"])) ["Hello" "Bye"])
;;
;; does not print anything because map is lazy in Clojure. It creates a sequence of operations but does
;;   not execute them until the sequence is consumed.
;; To force execution, you can use dorun or doseq, which are eager and execute side effects immediately:
;;
;; ME:
  (run! (fn [person] (run! (partial println person) ["Ernesto" "Daniel" "Virginia"])) ["Hello" "Bye"])
;; GPT:
  (run! (fn [person] (dorun (map (partial println person) ["Ernesto" "Daniel" "Virginia"]))) ["Hello" "Bye"])
;; GPT:
  (run! (fn [person] (doseq [name ["Ernesto" "Daniel" "Virginia"]] (println person name))) ["Hello" "Bye"])

;; difference of dorun, doseq, doall, run!
;;
;; https://stackoverflow.com/questions/25327369/what-is-the-difference-among-the-functions-doall-dorun-doseq-and-for
;; dorun, doall, and doseq are all for forcing lazy sequences, presumably to get side effects.
;;
;; dorun - don't hold whole seq in memory while forcing, return nil
;; doall - hold whole seq in memory while forcing (i.e. all of it) and return the seq
;; doseq - same as dorun, but gives you chance to do something with each element as it's forced returns nil
;; for is different in that it's a list comprehension, and isn't related to forcing effects. doseq and for have the same binding syntax, which may be a source of confusion, but doseq always returns nil, and for returns a lazy seq.

;; https://gist.github.com/finalfantasia/4b89a0c03752713743df8f16f298b166
;; If you have a lazy-seq with side-effects, you almost certainly don't want to let it out of your sight. You're likely to get very strange behavior unless you're exceedingly careful. Most likely, if you've got a lazy-seq with side-effects you should force it with dorun or doall immediately. Use doall if you care about the values in the produced lazy-seq, otherwise use dorun.
;; This means that dorun should almost always show up right next to the form producing the lazy-seq, which means doseq is very likely a better choice, as it is more efficient and usually more succinct than dorun combined with a lazy-seq producer.
;; run! is a convenience function that can take place of (dorun (map ,,,)).
;; for is in rather a different category, since unlike the others it produces a lazy-seq rather than forcing anything. Use for when it's a more convenient way to express the lazy-seq you want than the equivalent combination of map, filter, take-while, etc.
;; source: https://groups.google.com/d/msg/clojure/8ebJsllH8UY/FxbiHmwJE2oJ
  #_())
