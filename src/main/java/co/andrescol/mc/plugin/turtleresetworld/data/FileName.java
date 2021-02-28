package co.andrescol.mc.plugin.turtleresetworld.data;

/**
 * Enum that contains the list of files allowed to save
 */
public enum FileName {

    FILE_NAME_REGEN("regeneration_data_regen.yml"),
    FILE_NAME_COPY("regeneration_data_copy.yml");

    private final String fileName;

    FileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
