package model;

/**
 * The LoaderException class is a container and superclass to exceptions related to file loading.
 *
 * @author Samuel Henderson
 */
public class LoaderException extends Exception {

    protected double value;

    protected LoaderException(double value) {
        this.value = value;
    }

    protected LoaderException() {

    }

    /**
     * IntegralColorExceptions should be thrown when an integral value is not within the range [0, 255].
     */
    public static class IntegralColorException extends LoaderException {

        public IntegralColorException(double value) {
            super(value);
        }

        @Override
        public void printStackTrace() {
            System.out.println("Integral value " + (int) this.value + " exceeds required bounds: [0, 255]");
        }
    }

    /**
     * FileDimensionExceptions should be thrown when the length of an image file is not a perfect square.
     */
    public static class FileDimensionException extends LoaderException {

        public FileDimensionException(double value) {
            super(value);
        }

        @Override
        public void printStackTrace() {
            System.out.println("Nonsquare file dimension: " + this.value);
        }
    }

    /**
     * UnreadablePathExceptions should be thrown when file paths cannot be read or do not exist.
     */
    public static class UnreadablePathException extends LoaderException {

        private final String path;

        public UnreadablePathException(String path) {
            this.path = path;
        }

        @Override
        public void printStackTrace() {
            System.out.println("File or directory does not exist: " + this.path);
        }
    }

    /**
     * DirectoryCreationExceptions should be thrown during erroneous directory creations.
     */
    public static class DirectoryCreationException extends LoaderException {
        private final String path;

        public DirectoryCreationException(String path) {
            this.path = path;
        }

        @Override
        public void printStackTrace() {
            System.out.println("Failed to create missing directory: " + this.path);
        }
    }
}
