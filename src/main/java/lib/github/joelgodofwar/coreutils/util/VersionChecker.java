package lib.github.joelgodofwar.coreutils.util;

import lib.github.joelgodofwar.coreutils.util.common.PluginLogger;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VersionChecker {
    private final JavaPlugin plugin;
    private final PluginLogger logger;
    private final int projectID;
    private final String githubURL;
    private final String currentVersion;
    private final List<String> releaseList = new ArrayList<>();
    private final List<String> developerList = new ArrayList<>();
    private final List<MMHDLC> dlcList = new ArrayList<>();
    private String recommendedVersion = "uptodate";

    public VersionChecker(@NotNull JavaPlugin plugin, int projectID, @NotNull String githubURL) {
        Validate.notNull(plugin, "Plugin cannot be null");
        Validate.notNull(githubURL, "GitHub URL cannot be null");
        this.plugin = plugin;
        this.logger = new PluginLogger(plugin);
        this.projectID = projectID;
        this.githubURL = githubURL;
        this.currentVersion = plugin.getDescription().getVersion();
    }

    public VersionChecker(@NotNull String currentVersion, int projectID, @NotNull String githubURL, @NotNull PluginLogger logger) {
        Validate.notNull(currentVersion, "Current version cannot be null");
        Validate.notNull(githubURL, "GitHub URL cannot be null");
        Validate.notNull(logger, "Logger cannot be null");
        this.plugin = null;
        this.logger = logger;
        this.projectID = projectID;
        this.githubURL = githubURL;
        this.currentVersion = currentVersion;
    }

    public String getReleaseUrl() {
        return "https://dev.bukkit.org/projects/" + projectID;
    }

    public boolean checkForUpdates() throws Exception {
        URL url = new URL(githubURL);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
        Document doc = db.parse(connection.getInputStream());
        doc.getDocumentElement().normalize();

        NodeList releaseNodes = doc.getElementsByTagName("release");
        for (int i = 0; i < releaseNodes.getLength(); i++) {
            Node node = releaseNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                releaseList.add(element.getElementsByTagName("version").item(0).getTextContent().replace("<version>", "").replace("</version>", ""));
                releaseList.add(element.getElementsByTagName("notes").item(0).getTextContent().replace("<notes>", "").replace("</notes>", ""));
                releaseList.add(element.getElementsByTagName("link").item(0).getTextContent().replace("<link>", "").replace("</link>", ""));
            }
        }

        NodeList developerNodes = doc.getElementsByTagName("developer");
        for (int i = 0; i < developerNodes.getLength(); i++) {
            Node node = developerNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                developerList.add(element.getElementsByTagName("version").item(0).getTextContent().replace("<version>", "").replace("</version>", ""));
                developerList.add(element.getElementsByTagName("notes").item(0).getTextContent().replace("<notes>", "").replace("</notes>", ""));
                developerList.add(element.getElementsByTagName("link").item(0).getTextContent().replace("<link>", "").replace("</link>", ""));
            }
        }

            NodeList dlcNodes = doc.getElementsByTagName("dlcs");
            if (dlcNodes.getLength() > 0) {
                Node dlcNode = dlcNodes.item(0);
                if (dlcNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element dlcElement = (Element) dlcNode;
                    String dlcString = dlcElement.getTextContent().trim();
                    if (!Strings.isNullOrEmpty(dlcString)) {
                        String[] dlcParts = dlcString.split(",");
                        for (String part : dlcParts) {
                            String[] dlcDetails = part.split(";");
                            if (dlcDetails.length == 4) {
                                String filename = dlcDetails[0];
                                int numberOfFiles = NumberUtils.toInt(dlcDetails[1], 0);
                                double price = NumberUtils.toDouble(dlcDetails[2].replace("$", ""), 0.0);
                                String markerFile = dlcDetails[3];
                                dlcList.add(new MMHDLC(filename, numberOfFiles, price, markerFile, plugin));
                            }
                        }
                        logger.debug(ChatColor.YELLOW + "DLCs stored: %s", dlcList);
                    }
                }
            }


        if (connection != null) {
            connection.getInputStream().close();
        }

        String releaseVersion = releaseList.isEmpty() ? "" : releaseList.get(0);
        String developerVersion = developerList.isEmpty() ? "" : developerList.get(0);

        logger.debug(ChatColor.RED + "currentVersion=%s", currentVersion);
        logger.debug(ChatColor.RED + "releaseVersion=%s", releaseVersion);
        logger.debug(ChatColor.RED + "developerVersion=%s", developerVersion);

        if (currentVersion.compareTo(releaseVersion) < 0) {
            recommendedVersion = "release";
            return true;
        } else if (currentVersion.equals(releaseVersion)) {
            recommendedVersion = "uptodate";
            return false;
        } else if (currentVersion.contains(".D")) {
            String[] splitCurrentVersion = currentVersion.split("\\.D");
            String currentReleaseVersion = splitCurrentVersion[0];
            String[] splitDeveloperVersion = developerVersion != null ? developerVersion.split("\\.D") : new String[]{releaseVersion};
            String developerReleaseVersion = splitDeveloperVersion[0];
            if (currentReleaseVersion.equals(releaseVersion) && (developerVersion != null && developerReleaseVersion.compareTo(currentReleaseVersion) <= 0)) {
                recommendedVersion = "release";
                return true;
            } else if (developerVersion != null && developerReleaseVersion.compareTo(releaseVersion) < 0 && developerReleaseVersion.equals(currentReleaseVersion)) {
                recommendedVersion = "uptodate";
                return false;
            } else if (developerVersion != null && developerVersion.compareTo(currentVersion) > 0) {
                recommendedVersion = "developer";
                return true;
            }
        }

        recommendedVersion = "uptodate";
        return false;
    }

    public List<MMHDLC> getDLCList() {
        return Collections.unmodifiableList(dlcList);
    }

    public List<String> getReleaseList() {
        return new ArrayList<>(releaseList);
    }

    public List<String> getDeveloperList() {
        return new ArrayList<>(developerList);
    }

    public String getRecommendedVersion() {
        return recommendedVersion;
    }

    public String oldVersion() {
        return currentVersion;
    }

    public String newVersion() {
        if (recommendedVersion.equalsIgnoreCase("release")) {
            return releaseList.isEmpty() ? "UpToDate" : releaseList.get(0);
        } else if (recommendedVersion.equalsIgnoreCase("developer")) {
            return developerList.isEmpty() ? "UpToDate" : developerList.get(0);
        } else {
            return "UpToDate";
        }
    }

    public String newVersionNotes() {
        if (recommendedVersion.equalsIgnoreCase("release")) {
            return releaseList.size() < 2 ? "UpToDate" : releaseList.get(1);
        } else if (recommendedVersion.equalsIgnoreCase("developer")) {
            return developerList.size() < 2 ? "UpToDate" : developerList.get(1);
        } else {
            return "UpToDate";
        }
    }

    public String getDownloadLink() {
        if (recommendedVersion.equalsIgnoreCase("release")) {
            return releaseList.size() < 3 ? "UpToDate" : releaseList.get(2);
        } else if (recommendedVersion.equalsIgnoreCase("developer")) {
            return developerList.size() < 3 ? "UpToDate" : developerList.get(2);
        } else {
            return "UpToDate";
        }
    }
}