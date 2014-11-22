(ns pc.email
  (:require [clojure.string :as str]
            [clj-time.core :as time]
            [clj-time.format]
            [hiccup.core :as hiccup]
            [pc.mailgun :as mailgun]))

(defn chat-invite-html [doc-id]
  (hiccup/html
   [:html
    [:body
     [:p "Hey there,"]
     [:p
      "Come draw with me on Precursor: "
      [:a {:href (str "https://prcrsr.com/document/" doc-id)}
       (str "https://prcrsr.com/document/" doc-id)]]
     [:p
      [:a {:href (str "https://prcrsr.com/document/" doc-id)}
       [:img {:width 256
              :src (str "https://prcrsr.com/document/" doc-id ".png?printer-friendly=false")}]]]
     [:p {:style "font-size: 12px"}
      "If you think this message was an error, let us know: "
      [:a {:href "mailto:info@prcrsr.com"} "info@prcrsr.com"] "."
      ;; Add some hidden text so that Google doesn't try to trim these.
      [:span {:style "display: none; max-height: 0px; font-size: 0px; overflow: hidden;"}
       " Sent at "
       (clj-time.format/unparse (clj-time.format/formatters :rfc822) (time/now))
       "."]]]]))

(defn send-chat-invite [{:keys [cust to-email doc-id]}]
  (mailgun/send-message {:from "Precursor <draw@prcrsr.com>"
                         :to to-email
                         :subject (str/trim (str (:cust/first-name cust)
                                                 (when (and (:cust/first-name cust)
                                                            (:cust/last-name cust))
                                                   (str " " (:cust/last-name cust)))
                                                 " "
                                                 (cond (and (not (:cust/last-name cust))
                                                            (:cust/first-name cust))
                                                       (str "(" (:cust/email cust) ") ")

                                                       (not (:cust/first-name cust))
                                                       (str (:cust/email cust) " ")

                                                       :else nil)
                                                 "invited you to a document on Precursor"))
                         :text (str "Hey there,\nCome draw with me on Precursor: https://prcrsr.com/document" doc-id)
                         :html (chat-invite-html doc-id)}))
