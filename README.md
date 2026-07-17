# gitops-cicd-pipelines

Reusable CI/CD building blocks for shipping to Kubernetes: GitHub Actions
`workflow_call` templates, a Jenkins shared library for teams running
hybrid CI, and ready-to-use canary / blue-green deploy manifests.

## Why

Every service pipeline does the same four things — test, scan, build,
deploy — with an approval gate before production. Rewriting that per
repo drifts fast. This repo centralizes it so a single change (say,
tightening the Trivy severity threshold) propagates to every consumer.

## What's here

```
.github/workflows/
  reusable-build-test-scan.yml   test -> SonarQube gate -> multi-stage build -> Trivy scan -> push
  reusable-k8s-deploy.yml         helm upgrade --install --atomic -> rollout verification
  example-app-pipeline.yml        how an app repo wires the two together
jenkins/
  Jenkinsfile.example              pipeline using the shared library below
  vars/buildDockerImage.groovy      build + push + JFrog Artifactory build-info
  vars/deployToKubernetes.groovy    atomic helm deploy + rollout check
  vars/sonarQubeScan.groovy         quality-gate-enforced SonarQube step
k8s/
  canary-rollout-example.yaml        Argo Rollouts canary with Prometheus/Mimir analysis gate
  blue-green-service.yaml            zero-extra-tooling blue-green via Service selector flip
docs/
  pipeline-architecture.md            diagram + design rationale
```

## Using the GitHub Actions workflows from another repo

```yaml
jobs:
  ci:
    uses: jnnishad/gitops-cicd-pipelines/.github/workflows/reusable-build-test-scan.yml@main
    with:
      image_name: my-app
    secrets: inherit

  deploy:
    needs: ci
    uses: jnnishad/gitops-cicd-pipelines/.github/workflows/reusable-k8s-deploy.yml@main
    with:
      environment: production
      release_name: example-app
      chart_path: ./charts/example-app
      image_tag: ${{ github.sha }}
    secrets: inherit
```

## Using the Jenkins shared library

Point **Manage Jenkins → System → Global Pipeline Libraries** at this
repo (name it `gitops-pipelines`), then `@Library('gitops-pipelines') _`
at the top of any `Jenkinsfile` — see `jenkins/Jenkinsfile.example`.

## Deployment strategy

Zero-downtime by default: `helm upgrade --install --atomic` rolls back
automatically on failure, and `k8s/` ships both an Argo Rollouts canary
(with a Prometheus/Mimir-backed error-rate analysis gate) and a plain
blue-green Service pattern for clusters without progressive-delivery
tooling installed.

## Related repos

- [`k8s-observability-stack`](https://github.com/jnnishad/k8s-observability-stack) — the Mimir endpoint the canary analysis template queries
- [`terraform-multicloud-infra`](https://github.com/jnnishad/terraform-multicloud-infra) — the clusters these pipelines deploy to

## License

MIT — see [LICENSE](LICENSE).

<!-- JN -->

<!-- JN -->

<!-- JN -->
