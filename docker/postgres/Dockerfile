# Dockerfile postgres

FROM postgres:18

ARG PG_BIGM_VERSION=1.2-20250903

LABEL description="PostgreSQL ${PG_MAJOR} with pg_bigm ${PG_BIGM_VERSION}"

RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
        build-essential \
        postgresql-server-dev-${PG_MAJOR} \
        wget \
        ca-certificates \
        libicu-dev \
    ; \
    cd /tmp; \
    wget -q "https://github.com/pgbigm/pg_bigm/archive/refs/tags/v${PG_BIGM_VERSION}.tar.gz"; \
    tar -xzf "v${PG_BIGM_VERSION}.tar.gz"; \
    cd "pg_bigm-${PG_BIGM_VERSION}"; \
    make USE_PGXS=1; \
    make USE_PGXS=1 install; \
    test -f "/usr/lib/postgresql/${PG_MAJOR}/lib/pg_bigm.so" || exit 1; \
    test -f "/usr/share/postgresql/${PG_MAJOR}/extension/pg_bigm.control" || exit 1; \
    cd /; \
    rm -rf /tmp/*; \
    apt-get purge -y --auto-remove \
        build-essential \
        postgresql-server-dev-${PG_MAJOR} \
        wget \
    ; \
    apt-get clean; \
    rm -rf /var/lib/apt/lists/*

RUN echo "shared_preload_libraries = 'pg_bigm'" \
    >> /usr/share/postgresql/postgresql.conf.sample

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD pg_isready -U postgres || exit 1

