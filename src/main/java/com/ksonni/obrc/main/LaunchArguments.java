package com.ksonni.obrc.main;

record LaunchArguments(String path, BuilderType type) {
    enum BuilderType {
        SERIAL,
        FORK_JOIN,
        PLATFORM_THREADS,
        VIRTUAL_THREADS,
    }

    static LaunchArguments from(String[] args) throws IllegalArgumentException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Must specify file path as an argument");
        }
        var path = args[0];

        var type = BuilderType.VIRTUAL_THREADS;
        if (args.length > 1) {
            try {
                type = BuilderType.valueOf(args[1]);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid argument specified for builder type");
            }
        }

        System.out.printf("Using summary builder type: %s\n", type);

        return new LaunchArguments(path, type);
    }
}
