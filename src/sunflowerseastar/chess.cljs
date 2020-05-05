(ns sunflowerseastar.chess
  (:require
   [sunflowerseastar.helpers :refer [math-jax-wrapper]]
   [sunflowerseastar.components :refer [clojure-code iframe]]))

(defn chess []
  (math-jax-wrapper
   [:div.content
    (iframe "https://chess.sunflowerseastar.com"
            [:p.iframe-note [:strong "enter"] ": " [:em "computer move"] ", "
             [:strong "left"] "/" [:strong "right"] " or "
             [:strong "cmd+z"] "/" [:strong "shift+cmd+z"] ": " [:em  "undo"] "/" [:em "redo"] ", "
             [:strong "r"] ": " [:em "restart"]])
    [:div.content-inner
     [:h2 "Chess"]
     [:h3 "Goal"]
     [:p "To write a " [:a {:href "https://chess.sunflowerseastar.com" :rel "noreferrer" :target "_blank"} "chess app"] ", GUI & engine. Also, to improve at writing Clojure. And have fun!"]
     [:h3 "Setup"]
     [:p "So here's the board's data structure—" [:em "prepare to marvel"] ". (Just kidding, prepare to say, “You should be using a " [:a {:href "https://www.chessprogramming.org/Bitboards" :rel "noreferrer" :target "_blank"} "bitboard"] "!”)"]

     [:pre
      [:span.code-label "clojure"]
      [:code.clojure "
(defn generate-board []
  [(vec (map #(hash-map :color 'b :piece-type %1 :x %2 :y 0) ['r 'n 'b 'q 'k 'b 'n 'r] (range 0 8)))
   (vec (for [x (range 0 8)] {:color 'b :piece-type 'p :x x :y 1}))
   (vec (repeat 8 {}))
   (vec (repeat 8 {}))
   (vec (repeat 8 {}))
   (vec (repeat 8 {}))
   (vec (for [x (range 0 8)] {:color 'w :piece-type 'p :x x :y 6}))
   (vec (map #(hash-map :color 'w :piece-type %1 :x %2 :y 7) ['r 'n 'b 'q 'k 'b 'n 'r] (range 0 8)))])
"]]
     [:p.note "At the end of the day, this felt intuitive to me. Since I was able to conceptualize most of the functions in the implementation with this structure, I proceeded with it as a first pass."]
     [:p "There are three top-level atoms of state: " [:em "game"] ", " [:em "score"] ", and " [:em "ui"] "."]
     [:pre
      [:span.code-label "clojure"]
      [:code.clojure "
(def game (atom game-initial-state))
(def score (atom score-initial-state))
(def ui (atom {:is-info-page-showing false :has-initially-loaded false}))
"]]

     [:h3 "UI"]


     [:p "The board is a grid, generated by mapping through the board vector to place the squares and pieces."]

     [:pre
      [:span.code-label "clojure"]
      [:code.clojure "
[:div.board {:class [(if (= (@game :result) :checkmate) (str current-winner \" checkmate\") turn)
                     (if (= (@game :result) :draw) \"draw\")
                     (if (not-empty active-piece) \"is-active\")
                     (if stopped \"stopped\")
                     (if off \"off\")]}
 (map-indexed
  (fn [y row]
    (map-indexed
     (fn [x square]
       (let [{:keys [color piece-type]} square
             is-state-rest (= state :rest)
             is-current-color-turn (= turn color)
             can-activate (and is-state-rest is-current-color-turn)
             is-active (and (= (active-piece :x) x) (= (active-piece :y) y))]
         [:div.square
          {:key (str x y)
           :class [(if (or (and (even? y) (odd? x)) (and (odd? y) (even? x))) \"dark\")
                   (if can-activate \"can-activate\")
                   (if is-active \"active\")
                   (if (and (= king-x x) (= king-y y)) \"in-check\")]
           :style {:grid-column (+ x 1) :grid-row (+ y 1)}}
          (if (not-empty square)
            [:span.piece-container
             {:class [color piece-type]} (svg-of piece-type color)])]))
     row))
  board)])]
"]]
     [:p.note "Translating parts of the game state into conditional classnames for CSS is straight-forward."]

     [:p "Let’s hear it for grid, everybody!"]

     [:pre
      [:span.code-label "css"]
      [:code.css "
.board {
  display: grid;
  grid-template-columns: repeat(8, 12.5%);
}
"]]
     [:p.note "I loved how the grid only took three lines of code: the " [:em ":style"] " attribute inside of " [:a {:href "https://github.com/weavejester/hiccup" :rel "noreferrer" :target "_blank"} "hiccup"] "’s " [:em ":div.square"] ", and then these two lines of CSS."]

     [:p "Predicates distill the game state, and inform behaviors & event permissions inside click handlers."]

     [:pre
      [:span.code-label "clojure"]
      [:code.clojure "
  :on-click #(cond can-activate (activate-piece! square x y)
                  is-active (clear-active-piece!)
                  is-state-moving
                  (if (and (is-legal? active-piece x y board en-passant-target)
                            (not (in-check? (@game :turn) (board-after-move active-piece x y board) en-passant-target)))
                    (land-piece! active-piece x y)
                    (clear-active-piece!)))}
"]]

     [:p "The same predicates provide state-based styles, which are used as visual/UX cues."]
     [:pre
      [:span.code-label "css"]
      [:code.css "
.square.can-activate:hover:after {
  opacity: 1;
}
.square.can-activate {
  cursor: pointer;
}
"]]





     [:h3 "Legality"]

     [:p "Most of the nitty-gritty logic is in " [:a {:href "https://github.com/sunflowerseastar/chess/blob/master/src/chess/legal.cljs" :rel "noreferrer" :target "_blank"} "legal.cljs"] ". Here’s a knight:"]

     [:pre
      [:span.code-label "clojure"]
      [:code.clojure "
(defn is-legal-knight-move? [start-x start-y end-x end-y]
  (let [x-distance (get-distance start-x end-x)
        y-distance (get-distance start-y end-y)]
    (or (and (= x-distance 2) (= y-distance 1)) (and (= x-distance 1) (= y-distance 2)))))
"]]

     [:p "and a king:"]
     [:pre
      [:span.code-label "clojure"]
      [:code.clojure "
(defn is-legal-king-move? [start-x start-y end-x end-y]
  (let [x-distance (get-distance start-x end-x)
        y-distance (get-distance start-y end-y)
        is-one-square-move (and (<= x-distance 1) (<= y-distance 1))]
    is-one-square-move))
"]]
     [:p "There’s no state, so the repl shines. Programming is fun. Repl-driven programming is fun. Using " [:a {:href "https://clojure.org/guides/threading_macros" :rel "noreferrer" :target "_blank"} "threading macros"] " is fun.  = )"]
     [:pre
      [:span.code-label "clojure"]
      [:code.clojure "
(defn is-legal-diagonal-move? [start-x start-y end-x end-y board]
(let [x-distance (get-distance start-x end-x)
      y-distance (get-distance start-y end-y)
      xs (my-inclusive-range start-x end-x)
      ys (my-inclusive-range start-y end-y)
      interim-diagonals (->> (map #(get-piece %2 %1 board) ys xs) (drop 1) drop-last)
      interim-diagonals-are-open (every? empty? interim-diagonals)]
(and (= x-distance y-distance) interim-diagonals-are-open)))
"]]

     [:p "Here’s the centralized legality-checking function."]
     [:pre
      [:span.code-label "clojure"]
      [:code.clojure "
(defn is-legal?
\"Take an active (moving) piece, landing position, board, and en-passant-target.
       Return bool on whether the move is permitted.\"
[{:keys [color piece-type x y]} end-x end-y board en-passant-target]
(let [onto-same-color (= color (get-color end-x end-y board))]
  (cond onto-same-color false
        (= piece-type 'p) (is-legal-pawn-move? color x y end-x end-y board en-passant-target)
        (= piece-type 'r) (is-legal-cardinal-move? x y end-x end-y board)
        (= piece-type 'n) (is-legal-knight-move? x y end-x end-y)
        (= piece-type 'b) (is-legal-diagonal-move? x y end-x end-y board)
        (= piece-type 'q) (or (is-legal-cardinal-move? x y end-x end-y board)
                              (is-legal-diagonal-move? x y end-x end-y board))
        (= piece-type 'k) (is-legal-king-move? x y end-x end-y)
        :else false)))
"]]
     [:p.note "Once the ‘legality’ was written for the rook and bishop, I could throw a " [:em "or"] " at the queen and call it a day. Sometimes things that should be easy actually turn out to be easy. Not often. But more often in Clojure than not-Clojure, it seems."]


     [:p "I wrote a translation layer between my board + castling + en-passant state configuration to " [:a {:href "https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation" :rel "noreferrer" :target "_blank"} "FEN"] ". This helped shake out bugs. Here’s a fun one for each way: from fen $\\rightarrow$ my data structure, and my data structure $\\rightarrow$ fen."]


     [:pre
      [:span.code-label "clojure"]
      [:code.clojure "
(defn algebraic-notation->x-y [square]
  (let [x (->> (re-find #\"\\w\" square) (index-of \"abcdefgh\"))
        y (- 8 (re-find #\"\\d\" square))]
    (if (and x y) {:x x :y y} {:x -1 :y -1})))

(defn fen->my-en-passant [fen]
  (-> fen (split #" ") (nth 3) algebraic-notation->x-y))

(defn my-en-passant->fen-en-passant [{:keys [x y]}]
  (if (= x -1) \"-\"
      (let [file ((vec \"abcdefgh\") x)
            rank (- 8 y)]
        (str file rank))))
"]]
     [:p.note "In my game state logic, {:x -1 :y -1} represents ‘no en passant.’"]

     [:h3 "Result"]
     [:p "It’s no Stockfish, but it was a whirlwind romp that fit a full rule set, GUI, and engine in 600 lines of Clojure and 460 lines of CSS. If you press " [:strong "spacebar"] " during desktop play, computer moves will follow a limited openings table from Eduard Gufeld. There are a few basic heuristics for middle game play. Press the " [:strong "left"] " & " [:strong "right"] " arrow keys, or " [:strong "ctrl/cmd + Z"] " & " [:strong "ctrl/cmd + shift Z"] " for undo and redo. Click on the rook-ish hamburger for a stats screen, and you can copy or paste FENs to save your place or jump to a game position."]

     [:div.center
      [:a.stand-alone {:href "https://chess.sunflowerseastar.com" :rel "noreferrer" :target "_blank"} "chess"]
      [:a.stand-alone {:href "https://github.com/sunflowerseastar/chess/blob/master/src/chess/core.cljs" :rel "noreferrer" :target "_blank"} "code"]]


     ]]))