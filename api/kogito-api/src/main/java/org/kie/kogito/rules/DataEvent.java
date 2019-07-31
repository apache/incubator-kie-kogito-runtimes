package org.kie.kogito.rules;

public interface DataEvent<T> {

    class Insert<T> implements DataEvent<T> {

        private final DataHandle handle;
        private final T value;
        private final DataProcessor<T> sender;

        public Insert(DataHandle handle, T value, DataProcessor<T> sender) {
            this.handle = handle;
            this.value = value;
            this.sender = sender;
        }

        public Insert(T value) {
            this(null, value, null);
        }

        public DataHandle handle() {
            return handle;
        }

        public T value() {
            return value;
        }

        public DataProcessor<T> sender() {
            return sender;
        }
    }

    class Update<T> implements DataEvent<T> {

        private final DataHandle handle;

        private final T value;

        public Update(DataHandle handle, T value) {
            this.handle = handle;
            this.value = value;
        }

        public DataHandle handle() {
            return handle;
        }

        public T value() {
            return value;
        }
    }

    class Delete<T> implements DataEvent<T> {

        private final DataHandle handle;

        public Delete(DataHandle handle) {
            this.handle = handle;
        }

        public DataHandle handle() {
            return handle;
        }
    }
}