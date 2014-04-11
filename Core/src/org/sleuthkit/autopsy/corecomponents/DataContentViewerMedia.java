/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011-2013 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.corecomponents;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.imageio.ImageIO;

import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataContentViewer;
import org.sleuthkit.autopsy.coreutils.ImageUtils;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData.TSK_FS_NAME_FLAG_ENUM;

/**
 * Media content viewer for videos, sounds and images.
 */
@ServiceProviders(value = {
    @ServiceProvider(service = DataContentViewer.class, position = 5)
})
public class DataContentViewerMedia extends javax.swing.JPanel implements DataContentViewer {

    private static final String[] AUDIO_EXTENSIONS = new String[]{".mp3", ".wav", ".wma"};
    private static final Logger logger = Logger.getLogger(DataContentViewerMedia.class.getName());
    private AbstractFile lastFile;
    //UI
    private final MediaViewVideoPanel videoPanel;
    private final String[] videoExtensions; // get them from the panel
    private String[] imageExtensions; // use javafx supported 
    private final List<String> supportedMimes;
    private final MediaViewImagePanel imagePanel;
    private boolean videoPanelInited;
    private boolean imagePanelInited;
    private static final String IMAGE_VIEWER_LAYER = "IMAGE";
    private static final String VIDEO_VIEWER_LAYER = "VIDEO";

    /**
     * Creates new form DataContentViewerVideo
     */
    public DataContentViewerMedia() {

        initComponents();

        // get the right panel for our platform
        videoPanel = MediaViewVideoPanel.createVideoPanel();

        imagePanel = new MediaViewImagePanel();
        videoPanelInited = videoPanel.isInited();
        imagePanelInited = imagePanel.isInited();
    
        videoExtensions = videoPanel.getExtensions();
        supportedMimes = videoPanel.getMimeTypes();
        customizeComponents();
        logger.log(Level.INFO, "Created MediaView instance: " + this);
    }

    private void customizeComponents() {
        //initialize supported image types
        //TODO use mime-types instead once we have support
        String[] fxSupportedImagesSuffixes = ImageIO.getReaderFileSuffixes();
        imageExtensions = new String[fxSupportedImagesSuffixes.length];
        //logger.log(Level.INFO, "Supported image formats by javafx image viewer: ");
        for (int i = 0; i < fxSupportedImagesSuffixes.length; ++i) {
            String suffix = fxSupportedImagesSuffixes[i];
            //logger.log(Level.INFO, "suffix: " + suffix);
            imageExtensions[i] = "." + suffix;
        }

        add(imagePanel, IMAGE_VIEWER_LAYER);
        add(videoPanel, VIDEO_VIEWER_LAYER);

        switchPanels(false);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.CardLayout());
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DataContentViewerMedia.class, "DataContentViewerMedia.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void setNode(Node selectedNode) {
        try {
            if (selectedNode == null) {
                resetComponent();
                return;
            }

            AbstractFile file = selectedNode.getLookup().lookup(AbstractFile.class);
            if (file == null) {
                resetComponent();
                return;
            }

            if (file.equals(lastFile)) {
                return; //prevent from loading twice if setNode() called mult. times
            }

            lastFile = file;

            final Dimension dims = DataContentViewerMedia.this.getSize();
            logger.info("setting node on media viewer");
            if (imagePanelInited && containsExt(file.getName(), imageExtensions)) {
                imagePanel.showImageFx(file, dims);
                this.switchPanels(false);
            } else if (imagePanelInited && ImageUtils.isJpegFileHeader(file)) {

                imagePanel.showImageFx(file, dims);
                this.switchPanels(false);

            } else if (videoPanelInited
                    && containsMimeType(selectedNode,supportedMimes)&&(containsExt(file.getName(), videoExtensions) || containsExt(file.getName(), AUDIO_EXTENSIONS))) {
                videoPanel.setupVideo(file, dims);
                switchPanels(true);
                
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception while setting node", e);
        }
    }

    /**
     * switch to visible video or image panel
     *
     * @param showVideo true if video panel, false if image panel
     */
    private void switchPanels(boolean showVideo) {
        CardLayout layout = (CardLayout) this.getLayout();
        if (showVideo) {
            layout.show(this, VIDEO_VIEWER_LAYER);
        } else {
            layout.show(this, IMAGE_VIEWER_LAYER);
        }
    }

    @Override
    public String getTitle() {
        return NbBundle.getMessage(this.getClass(), "DataContentViewerMedia.title");
    }

    @Override
    public String getToolTip() {
        return NbBundle.getMessage(this.getClass(), "DataContentViewerMedia.toolTip");
    }

    @Override
    public DataContentViewer createInstance() {
        return new DataContentViewerMedia();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void resetComponent() {
        videoPanel.reset();
        imagePanel.reset();
        lastFile = null;
    }

    @Override
    public boolean isSupported(Node node) {
        if (node == null) {
            return false;
        }

        AbstractFile file = node.getLookup().lookup(AbstractFile.class);
        if (file == null) {
            return false;
        }

        if (file.getSize() == 0) {
            return false;
        }
        String name = file.getName().toLowerCase();
        if (imagePanelInited) {
            if (containsExt(name, imageExtensions)) {
                return true;
            }
            else if (ImageUtils.isJpegFileHeader(file)) {
                return true;
            }
            //for gstreamer formats, check if initialized first, then
            //support audio formats, and video formats
        } 
        
        if (videoPanelInited && videoPanel.isInited()) {
            if ((containsExt(name, AUDIO_EXTENSIONS)
                || containsExt(name, videoExtensions))&& containsMimeType(node,supportedMimes)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int isPreferred(Node node) {
        //special case, check if deleted video, then do not make it preferred
        AbstractFile file = node.getLookup().lookup(AbstractFile.class);
        if (file == null) {
            return 0;
        }
        String name = file.getName().toLowerCase();
        boolean deleted = file.isDirNameFlagSet(TSK_FS_NAME_FLAG_ENUM.UNALLOC);

        if (containsExt(name, videoExtensions) && deleted) {
            return 0;
        } 
        else {
            return 7;
        }
        
    }

    private static boolean containsExt(String name, String[] exts) {
        int extStart = name.lastIndexOf(".");
        String ext = "";
        if (extStart != -1) {
            ext = name.substring(extStart, name.length()).toLowerCase();
        }
        return Arrays.asList(exts).contains(ext);
    }
     private static boolean containsMimeType(Node node, List<String> mimeTypes) {
         if (mimeTypes.isEmpty()) return true; //GStreamer currently is empty. Signature detection for javafx currently
         AbstractFile file = node.getLookup().lookup(AbstractFile.class);   
            try {
            ArrayList<BlackboardAttribute> genInfoAttributes = file.getGenInfoAttributes(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_FILE_TYPE_SIG);
            if (genInfoAttributes.isEmpty() == false) {
                for (BlackboardAttribute batt : genInfoAttributes) {
                    if (mimeTypes.contains(batt.getValueString())) {
                        return true;
                    }
                }
                return false;
            }
        } catch (TskCoreException ex) {
            return false;
        }
         return false;
     }
}
