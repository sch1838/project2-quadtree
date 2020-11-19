package model;

public class LoaderException extends Exception {

    protected double value;

    protected LoaderException(double value) {
        this.value = value;
    }

    protected LoaderException() {

    }

    public static class IntegralColorException extends LoaderException {

        public IntegralColorException(double value) {
            super(value);
        }

        @Override
        public void printStackTrace() {
            System.out.println("Integral value " + (int) this.value + " exceeds required bounds: [0, 255]");
        }
    }

    public static class FileDimensionException extends LoaderException {

        public FileDimensionException(double value) {
            super(value);
        }

        @Override
        public void printStackTrace() {
            System.out.println("Nonsquare file dimension: " + this.value);
        }
    }

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
