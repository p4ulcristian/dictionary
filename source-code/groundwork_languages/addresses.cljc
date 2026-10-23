(ns groundwork-languages.addresses
  "A language in an address, the same on both sides: the first part of the
   path, /de/orders, when more than one language shows (the launched ones
   and the preview ones this environment shows). The default language's
   addresses carry none, /orders; one given anyway, /en/orders, is read as
   it."
  (:require
   [groundwork-languages.settings :as settings]))

(defn- has-several-languages? [settings]
  (< 1 (count (settings/list-live-languages settings))))

(defn split-language-from-path
  "{:language \"de\" :path \"/orders\"} for /de/orders; {:language nil
   :path path} for a path that starts with none of the languages, and
   always with one language."
  [settings path]
  (let [[_ first-part more] (re-matches #"/([^/]+)(/.*)?" path)]
    (if (and (has-several-languages? settings) first-part (some #{first-part} (settings/list-live-languages settings)))
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
