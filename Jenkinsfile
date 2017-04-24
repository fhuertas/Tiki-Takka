@Library('libpipelines@branch-0.3') _

hose {
    EMAIL = 'governance'
    LANG = 'scala'
    SLACKTEAM = 'data-governance'
    MODULE = 'tiki-takka'
    REPOSITORY = 'tiki-takka'
    DEVTIMEOUT = 30
    RELEASETIMEOUT = 30
    MAXITRETRIES = 2
    EXPOSED_PORTS = [8080]

    FOSS = true

    ITSERVICES = [
        ['CONSUL': [
            'image': 'consul:0.7.0',
            'sleep': 20,
            'healthcheck': 8500
        ]],
        ['ZOOKEEPER': [
            'image': 'jplock/zookeeper:3.5.2-alpha',
            'sleep': 10,
            'healthcheck': 2181
        ]],
        ['MESOS-MASTER': [
            'image': 'mesosphere/mesos-master:1.0.1-2.0.93.ubuntu1404',
            'sleep': 10,
            'healthcheck': 5050,
            'env': [
                'MESOS_ZK=zk://%%ZOOKEEPER:2181/mesos',
                'MESOS_QUORUM=1',
                'MESOS_CLUSTER=%%JUID',
                'MESOS_REGISTRY=replicated_log'
            ]
        ]],
        ['MESOS-SLAVE': [
            'image': 'mesosphere/mesos-slave:1.0.1-2.0.93.ubuntu1404',
            'env': [
                'MESOS_MASTER=zk://%%ZOOKEEPER:2181/mesos',
                'MESOS_CONTAINERIZERS=docker,mesos',
                'MESOS_PORT=5051',
                'MESOS_RESOURCES=ports\\(*\\):[11000-11999]',
                'MESOS_WORK_DIR=/tmp/mesos',
                'MESOS_HOSTNAME=%%MESOS-SLAVE'
            ]
        ]],
        ['MARATHON': [
            'image': 'mesosphere/marathon:v1.3.0',
            'cmd': '--zk zk://%%ZOOKEEPER:2181/mesos',
            'sleep': 10,
            'env': [
                'MARATHON_MASTER=zk://%%ZOOKEEPER:2181/mesos'
            ]
        ]]
    ]

    ITPARAMETERS = """
        | -Dconsul.uri="http://%%CONSUL:8500"
        | -Dmarathon.uri="http://%%MARATHON:8080"
        | """

    DEV = {
        config ->
            doCompile(config)
            parallel(UT: {doUT(config)},
                     IT: {doIT(config)},
                     failFast: config.FAILFAST)

            doPackage(config)

            parallel(QC: {doStaticAnalysis(config)},
                     DEPLOY: {doDeploy(config)},
                     failFast: config.FAILFAST)
    }
}
