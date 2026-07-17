# Pipeline architecture

```
   PR opened ──► reusable-build-test-scan.yml
                    │  unit tests, SonarQube gate,
                    │  multi-stage Docker build,
                    │  Trivy CRITICAL/HIGH scan (blocking)
                    ▼
   merge to main ──► image pushed (sha + latest tags)
                    ▼
              reusable-k8s-deploy.yml (environment: staging)
                    │  helm upgrade --install --atomic
                    │  kubectl rollout status (auto-rollback on failure)
                    ▼
        GitHub Environment approval gate (production)
                    ▼
              reusable-k8s-deploy.yml (environment: production)
                    │  same chart, new image tag
                    ▼
          Argo Rollouts canary OR blue-green Service flip
```

**Why reusable workflows, not copy-pasted YAML per repo:** the build/test/
scan and deploy logic is identical across every service; only the image
name, chart path, and environment differ. Reusable workflows (`workflow_call`)
keep that logic in one place — a fix to the Trivy scan step lands in every
consuming repo the next time their pipeline runs, with no per-repo PRs.

**Why `--atomic` on every Helm deploy:** a failed upgrade auto-rolls back
to the last working release instead of leaving the namespace half-updated,
which is what actually causes 2am pages.

**Why both Argo Rollouts and a plain blue-green example:** not every
cluster has Argo Rollouts installed. The blue-green Service pattern in
`k8s/blue-green-service.yaml` needs nothing beyond core Kubernetes and is
what a smaller team can adopt on day one; the canary example is the
upgrade path once progressive-delivery tooling is in place.
