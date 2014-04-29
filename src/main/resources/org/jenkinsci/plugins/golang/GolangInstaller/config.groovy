package org.jenkinsci.plugins.golang

def f = namespace(lib.FormTagLib)

f.entry(title: _("Version"), field: "id") {

    def releases = descriptor.installableReleases
    if (releases.isEmpty()) {
        f.textbox()
    } else {
        select(name: ".id") {
            releases.each { release ->
                f.option(value: release.id, selected: release.id == instance?.id, release.name)
            }
        }
    }

}
