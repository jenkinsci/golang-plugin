package org.jenkinsci.plugins.golang

def f = namespace(lib.FormTagLib)

def releases = descriptor.installableReleases
if (releases == null || releases.isEmpty()) {
    f.block {
        text(_('Go version information has not been downloaded. ' +
                'To do so, press "Check now" in the Plugin Manager, or restart Jenkins.'))
    }
} else {
    f.entry(title: _("Version"), field: "id") {
        select(name: ".id") {
            releases.each { release ->
                f.option(value: release.id, selected: release.id == instance?.id, release.name)
            }
        }
    }
}
