SELECT EXISTS(
    SELECT FROM pg_tables WHERE tablename  = 'process_instances'
    );