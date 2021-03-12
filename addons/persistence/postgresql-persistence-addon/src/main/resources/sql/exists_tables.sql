SELECT EXISTS(
    SELECT FROM pg_tables
             WHERE  schemaname = 'public'
             AND    tablename  = 'process_instances'
             )