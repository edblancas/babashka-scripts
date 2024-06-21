#!/usr/bin/env bb

(require '[babashka.fs :as fs])
(require '[babashka.process :refer [shell]])

(let [fonts (map str (fs/glob "." "*.ttf"))
      patch-cmds (map #(str "fontforge -script font-patcher " %1 " -c") fonts)]
  (doseq [cmd patch-cmds]
    (shell cmd)))
