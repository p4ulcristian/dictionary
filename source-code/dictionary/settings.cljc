(ns dictionary.settings
  "dictionary's settings, as a malli shape, checked before anything is
   read. Nothing has a default: every key is written out, nil where it is
   a choice, and a missing or unknown one refuses, named, with every other
   problem at once (:dictionary/bad-settings)."
  (:require
   [malli.core :as malli]
   [malli.error :as malli-error]))

(def ^:private language-tag
  [:re {:error/message "should be a language tag in lowercase: \"en\", \"de\", \"pt-br\""}
   #"^[a-z]{2,3}(-[a-z0-9]{2,8})*$"])

(defn list-live-languages
  "The languages that show here: the launched :languages, then the
   :preview-languages this environment shows."
  [{:keys [languages preview-languages]}]
  (into (vec languages) preview-languages))

(defn- are-both-lists? [{:keys [languages preview-languages]}]
  (and (vector? languages) (vector? preview-languages)))

(defn- is-default-one-of-the-languages? [{:keys [languages default]}]
  (or (not (vector? languages)) (not (string? default)) (some #{default} languages)))

(defn- is-fallback-a-live-language? [{:keys [fallback-language] :as settings}]
  (or (not (are-both-lists? settings)) (not (string? fallback-language))
      (some #{fallback-language} (list-live-languages settings))))

(defn- are-languages-distinct? [settings]
  (or (not (are-both-lists? settings))
      (let [live (list-live-languages settings)] (= (count live) (count (distinct live))))))

(def settings-shape
  "The settings, every key required.
   :languages: the app's languages, as lowercase tags, [\"en\" \"de\"].
   :default: one of them: the language whose addresses have no prefix
   (/orders, while German's are /de/orders), and the one a request is in
   when nothing else says.
   :cookie-name: the cookie a person's chosen language is read from (the
   app writes it), or nil for none.
   :preview-languages: languages not launched yet that show here, in this
   environment (a stage where translators review them), [] for none (a
   production that shows only the launched ones). A language that shows
   is read from addresses, cookies and Accept-Language, and has a catalog.
   :fallback-language: the language whose text a message missing from
   another catalog is given in; one of the languages that show.
   :catalogs-path: where the catalogs are, \"i18n\": each language's
   <catalogs-path>/<language>.json, a classpath resource for the server,
   and the same file at /<catalogs-path>/<language>.json for the browser."
  [:and
   [:map {:closed true}
    [:languages [:vector {:min 1} language-tag]]
    [:default language-tag]
    [:cookie-name [:maybe [:string {:min 1}]]]
    [:preview-languages [:vector language-tag]]
    [:fallback-language language-tag]
    [:catalogs-path [:re {:error/message "should be a folder's path, \"i18n\" or \"app/i18n\", no / at either end"}
                     #"^[A-Za-z0-9._-]+(/[A-Za-z0-9._-]+)*$"]]]
   [:fn {:error/message ":default is one of :languages, the launched ones"} is-default-one-of-the-languages?]
   [:fn {:error/message ":fallback-language is one of :languages or :preview-languages"} is-fallback-a-live-language?]
   [:fn {:error/message ":languages and :preview-languages name each language once"} are-languages-distinct?]])

(defn check-settings
  "Returns the settings; throws {:type :dictionary/bad-settings :errors …}
   with every problem at once when they don't fit."
  [settings]
  (when-let [explanation (malli/explain settings-shape settings)]
    (let [errors (malli-error/humanize explanation)]
      (throw (ex-info (str "The settings have problems: " (pr-str errors))
                      {:type :dictionary/bad-settings :errors errors}))))
  settings)
