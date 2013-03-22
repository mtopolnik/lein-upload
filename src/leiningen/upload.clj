(ns leiningen.upload
  (require (leiningen [deploy :refer [repo-for]])
           [leiningen.core.main :as main]
           (clojure.java [shell :as sh] [io :as io])
           [clojure.string :as s]
           [cemerick.pomegranate.aether :as aether])
  (import cemerick.pomegranate.aether.PomegranateWagonProvider
          org.apache.maven.wagon.Wagon
          org.apache.maven.wagon.repository.Repository
          org.apache.maven.wagon.authentication.AuthenticationInfo))

(set! *warn-on-reflection* true)

(defn abort [fmt & args] (main/abort (apply format fmt args)))

(defn sh! [& args]
  (apply println "$" args)
  (let [res (apply sh/sh args)]
    (print (:out res)) (print (:err res)) (flush)
    (when-not (zero? (:exit res)) (abort "Command failed with exit code %s: %s" (:exit res) args))))

#_
(defn aether-deploy [project file]
  (let [files {[(symbol (:group project) (:name project)) (:version project) :extension "tgz"]
               file}]
    (aether/deploy-artifacts
     :artifacts (keys files)
     :files files
     :transfer-listener :stdout
     :repository [(leiningen.deploy/repo-for project "releases")])))

(defn upload
  "Upload a file to a repository"
  [project filename reponame]
  (let [f (io/file filename)
        repo (second (leiningen.deploy/repo-for project reponame))
        repo-obj (Repository. "" (:url repo))
        proto (.getProtocol repo-obj)
        wagon (.lookup (PomegranateWagonProvider.) proto)]
    (println "Upload" filename "==>" (:url repo))
    (cond
     wagon (doto wagon
             (.connect repo-obj (doto (AuthenticationInfo.)
                                  (.setUserName (:username repo))
                                  (.setPassword (:password repo))
                                  (.setPassphrase (:passphrase repo))))
             (.put f (.getName f)))
     (= proto "forge") (sh! "rsync" "-e" "ssh" (.getPath f)
                            (format "%s@frs.sourceforge.net:/home/frs/project/%s/"
                                    (:username repo) (.getHost repo-obj))))))
