package model;

/**
 * The LoaderException class is a container and superclass to exceptions related to file loading.
 *
 * @author Samuel Henderson
 */
public class LoaderException extends Exception {

    protected double value;

    protected String message;

    protected LoaderException(double value, String message) {
        this(message);
        this.value = value;
    }

    protected LoaderException(String message) {
        this.message = message;
    }

    @Override
    public void printStackTrace() {
        System.out.println(this.message);
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    /**
     * IntegralColorExceptions should be thrown when an integral value is not within the range [0, 255].
     */
    public static class IntegralColorException extends LoaderException {

        public IntegralColorException(double value) {
            super(value, "Integral value " + (int) value + " exceeds required bounds: [0, 255]");
        }
    }

    /**
     * FileDimensionExceptions should be thrown when the length of an image file is not a perfect square.
     */
    public static class FileDimensionException extends LoaderException {

        public FileDimensionException(double value) {
            super(value, "Nonsquare file dimension: " + value);
        }
    }

    /**
     * UnreadablePathExceptions should be thrown when file paths cannot be read or do not exist.
     */
    public static class UnreadablePathException extends LoaderException {

        private final String path;

        public UnreadablePathException(String path) {
            super("File or directory does not exist: " + path);
            this.path = path;
        }
    }

    /**
     * DirectoryCreationExceptions should be thrown during erroneous directory creations.
     */
    public static class FileCreationException extends LoaderException {
        private final String path;

        public FileCreationException(String path) {
            super("Failed to create missing directory: " + path);
            this.path = path;
        }
    }
}
