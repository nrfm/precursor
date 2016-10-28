(ns frontend.analytics
  (:require [frontend.analytics.mixpanel :as mixpanel]
            [frontend.analytics.rollbar :as rollbar]
            [frontend.state :as state]
            [frontend.utils :as utils :include-macros true]))

(defn init-user [c])
#_(defn init-user [cust]
  (utils/swallow-errors
   (rollbar/init (str (:cust/uuid cust)) (:cust/email cust)))
  (utils/swallow-errors
   (mixpanel/identify (str (:cust/uuid cust)))
   (mixpanel/name-tag (:cust/email cust))
   (mixpanel/set-people-props {:$email (:cust/email cust)
                               :$last_login (js/Date.)})))

(defn init-ab-tests [c])
#_(defn init-ab-tests [choices]
  (utils/swallow-errors
   (mixpanel/register-once choices)))

(defn init [s])
#_(defn init [state]
  (init-ab-tests (:ab-choices state))
  (when-let [cust (:cust state)]
    (init-user cust)))

(defn track-path [p])
#_(defn track-path [path]
  (mixpanel/track-pageview path))

(defn track-page [p & ps])
#_(defn track-page [page & [props]]
  (mixpanel/track page props))

(defn track [p & ps])
#_(defn track [page & [props]]
  (mixpanel/track page props))

(def controls-blacklist #{:chat-db-updated
                          :mouse-moved
                          :chat-body-changed
                          :default
                          :mouse-depressed
                          :mouse-released
                          :application-shutdown
                          :visibility-changed
                          :key-state-changed
                          :camera-nudged-right
                          :camera-nudged-left
                          :camera-nudged-down
                          :camera-nudged-up
                          :text-layer-edited
                          :layer-ui-id-edited
                          :layer-ui-target-edited
                          :subscriber-updated
                          :handle-camera-query-params
                          :media-stream-volume
                          :navigate-to-landing-doc-hovered
                          :make-button-hovered
                          :db-document-name-changed})

(defn track-control [e d s])
#_(defn track-control [event data state]
  (when-not (contains? controls-blacklist event)
    (mixpanel/track (str event) (merge
                                 (:analytics-data data)
                                 (dissoc (get-in state state/browser-settings-path) :document-settings)
                                 {:logged-in? (boolean (get-in state [:cust :cust/email]))}
                                 (when (:document/id state)
                                   {:doc-id (:document/id state)
                                    :subscriber-count (count (get-in state [:subscribers :info]))})))
    (mixpanel/set-people-props {(str event) true})))

(defn track-nav [e d s])
#_(defn track-nav [event data state]
  (mixpanel/track (str "Navigated to " event) (merge
                                               (:analytics-data data)
                                               (dissoc (get-in state state/browser-settings-path) :document-settings)
                                               {:logged-in? (boolean (get-in state [:cust :cust/email]))}
                                               (when (:document/id state)
                                                 {:doc-id (:document/id state)
                                                  :subscriber-count (count (get-in state [:subscribers :info]))})))
  (mixpanel/set-people-props {(str "Navigated to " event) true}))
