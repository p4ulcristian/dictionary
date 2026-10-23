(ns dictionary.texts
  "Which message a key is shown by, the same on both sides. A catalog is
   one language's flat map of ICU MessageFormat messages by key, as its
   JSON file holds it; the texts are every catalog the app loaded, with
   the fallback language:

     {:fallback-language \"en\"
      :catalogs          {\"en\" {\"orders.cart/add\" \"Add {count, plural, one {# item} other {# items}}\"}
                          \"de\" {\"orders.cart/add\" \"…\"}}}

   A key is a namespaced keyword, :orders.cart/add, found in the JSON as
   \"orders.cart/add\". A key missing from the language's catalog is found
   in the fallback language's; one missing there too is shown as the key's
   name, \"orders.cart/add\", ugly on purpose, so it is seen.

   A message's arguments are named by the args' keys without their
   namespace, a - read as _ (ICU's names take no -): {:retry-after-ms 1500}
   fills {retry_after_ms}."
  (:require
   [clojure.string :as str]))

(defn turn-key-into-catalog-key
  ":orders.cart/add -> \"orders.cart/add\"; a plain keyword, its name."
  [message-key]
  (if-let [key-namespace (namespace message-key)]
    (str key-namespace "/" (name message-key))
    (name message-key)))

(defn find-message
  "{:message … :language …}: the key's message in the language, else in
   the fallback language (and that language, for its plural rules); nil
   when neither has it."
  [{:keys [fallback-language catalogs]} language message-key]
  (let [catalog-key (turn-key-into-catalog-key message-key)]
    (or (when-let [message (get-in catalogs [language catalog-key])]
          {:message message :language language})
        (when-let [message (get-in catalogs [fallback-language catalog-key])]
          {:message message :language fallback-language}))))

(defn turn-key-into-argument-name
  "An args key as the message names it: its name without the namespace,
   each - a _, since ICU's argument names take no -:
   :order/shipped-at -> \"shipped_at\", :count -> \"count\"."
  [k]
  (str/replace (name k) "-" "_"))

(defn turn-args-into-names
  "The message's arguments by their names as text: {:count 3} -> {\"count\" 3},
   {:retry-after-ms 1500} -> {\"retry_after_ms\" 1500}."
  [args]
  (into {} (map (fn [[k v]] [(turn-key-into-argument-name k) v])) args))
