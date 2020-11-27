HOSTNAME=`hostname`

  OPTS=`getopt -o h: --long hostname: -n 'parse-options' -- "$@"`
  if [ $? != 0 ] ; then echo "Failed parsing options." >&2 ; exit 1 ; fi

  echo "$OPTS"
  eval set -- "$OPTS"

  while true; do
    case "$1" in
      -h | --hostname )     HOSTNAME=$2;        shift; shift ;;
      -- ) shift; break ;;
      * ) break ;;
    esac
  done
echo "Using HOSTNAME='$HOSTNAME'"

mongo localhost:27017/inventory <<-EOF
    rs.initiate({
        _id: "rs0",
        members: [ { _id: 0, host: "${HOSTNAME}:27017" } ]
    });
EOF
echo "Initiated replica set"

sleep 3
mongo localhost:27017/admin <<-EOF
    db.createUser({ user: 'admin', pwd: 'admin', roles: [ { role: "userAdminAnyDatabase", db: "admin" } ] });
EOF

mongo -u admin -p admin localhost:27017/admin <<-EOF
    db.runCommand({
        createRole: "listDatabases",
        privileges: [
            { resource: { cluster : true }, actions: ["listDatabases"]}
        ],
        roles: []
    });

    db.createUser({
        user: 'debezium',
        pwd: 'dbz',
        roles: [
            { role: "readWrite", db: "inventory" },
            { role: "read", db: "local" },
            { role: "listDatabases", db: "admin" },
            { role: "read", db: "config" },
            { role: "read", db: "admin" }
        ]
    });
EOF

echo "Created users"

mongo -u debezium -p dbz --authenticationDatabase admin localhost:27017/inventory <<-EOF
    use inventory;

    db.products.insert([
        { _id : NumberLong("101"), name : 'scooter', description: 'Small 2-wheel scooter', weight : 3.14, quantity : NumberInt("3") },
        { _id : NumberLong("102"), name : 'car battery', description: '12V car battery', weight : 8.1, quantity : NumberInt("8") },
        { _id : NumberLong("103"), name : '12-pack drill bits', description: '12-pack of drill bits with sizes ranging from #40 to #3', weight : 0.8, quantity : NumberInt("18") },
        { _id : NumberLong("104"), name : 'hammer', description: "12oz carpenter's hammer", weight : 0.75, quantity : NumberInt("4") },
        { _id : NumberLong("105"), name : 'hammer', description: "14oz carpenter's hammer", weight : 0.875, quantity : NumberInt("5") },
        { _id : NumberLong("106"), name : 'hammer', description: "16oz carpenter's hammer", weight : 1.0, quantity : NumberInt("0") },
        { _id : NumberLong("107"), name : 'rocks', description: 'box of assorted rocks', weight : 5.3, quantity : NumberInt("44") },
        { _id : NumberLong("108"), name : 'jacket', description: 'water resistent black wind breaker', weight : 0.1, quantity : NumberInt("2") },
        { _id : NumberLong("109"), name : 'spare tire', description: '24 inch spare tire', weight : 22.2, quantity : NumberInt("5") }
    ]);

    db.customers.insert([
        { _id : NumberLong("1001"), first_name : 'Sally', last_name : 'Thomas', email : 'sally.thomas@acme.com' },
        { _id : NumberLong("1002"), first_name : 'George', last_name : 'Bailey', email : 'gbailey@foobar.com' },
        { _id : NumberLong("1003"), first_name : 'Edward', last_name : 'Walker', email : 'ed@walker.com' },
        { _id : NumberLong("1004"), first_name : 'Anne', last_name : 'Kretchmar', email : 'annek@noanswer.org' }
    ]);

    db.orders.insert([
        { _id : NumberLong("10001"), order_date : new ISODate("2016-01-16T00:00:00Z"), purchaser_id : NumberLong("1001"), quantity : NumberInt("1"), product_id : NumberLong("102") },
        { _id : NumberLong("10002"), order_date : new ISODate("2016-01-17T00:00:00Z"), purchaser_id : NumberLong("1002"), quantity : NumberInt("2"), product_id : NumberLong("105") },
        { _id : NumberLong("10003"), order_date : new ISODate("2016-02-19T00:00:00Z"), purchaser_id : NumberLong("1002"), quantity : NumberInt("2"), product_id : NumberLong("106") },
        { _id : NumberLong("10004"), order_date : new ISODate("2016-02-21T00:00:00Z"), purchaser_id : NumberLong("1003"), quantity : NumberInt("1"), product_id : NumberLong("107") }
    ]);
EOF

echo "Inserted example data"
