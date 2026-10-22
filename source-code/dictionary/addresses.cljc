(ns dictionary.addresses
  "A language in an address, the same on both sides: the first part of the
   path, /de/orders, when the app has more than one language. The default
   language's addresses carry none, /orders; one given anyway, /en/orders,
   is read as it.")

(defn- has-several-languages? [{:keys [languages]}]
  (< 1 (count languages)))

(defn split-language-from-path
  "{:language \"de\" :path \"/orders\"} for /de/orders; {:language nil
   :path path} for a path that starts with none of the languages, and
   always with one language."
  [{:keys [languages] :as settings} path]
  (let [[_ first-part more] (re-matches #"/([^/]+)(/.*)?" path)]
    (if (and (has-several-languages? settings) first-part (some #{first-part} languages))
      {:language first-part :path (or more "/")}
      {:language nil :path path})))

(defn make-path-in-language
  "The path, which carries no language, in this one: /de/orders, or
   /orders for the default language (and always with one language); the
   home page, /, is /de."
  [{:keys [default] :as settings} path language]
  (if (or (= language default) (not (has-several-languages? settings)))
    path
    (str "/" language (when-not (= "/" path) path))))
