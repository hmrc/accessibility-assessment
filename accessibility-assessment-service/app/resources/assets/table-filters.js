$(document).ready(function () {
    const axeTable = $('#axeTable').DataTable(
        {
            "pageLength": 100,
        },
    ),
    vnuTable = $('#vnuTable').DataTable({
        "pageLength": 100,
        columnDefs: [
            {
                target: 1,
                visible: false,
                searchable: false,
            },
        ],
    });

    const axeContainer = $("#axeTable_wrapper");
    const axeShowOnlyErrorsFilter = $("<div class='filter'><label for='axe_show_only_errors'>Show only errors</label><input type='checkbox' id='axe_show_only_errors' data-table='axeTable'/></div>");
    const axeHideKnownIssuesFilter = $("<div class='filter'><label for='axe_hide_known_issues'>Hide known issues</label><input type='checkbox' id='axe_hide_known_issues'/></div>");
    axeContainer.prepend(axeShowOnlyErrorsFilter);
    axeContainer.prepend(axeHideKnownIssuesFilter);

    axeShowOnlyErrorsFilter.change(function () {filterTableErrors('axe_show_only_errors', axeTable);});
    axeHideKnownIssuesFilter.change(function () { filterTableKnownIssues('axe_hide_known_issues', axeTable);});

    const vnuContainer = $("#vnuTable_wrapper");
    const vnuShowOnlyErrorsFilter = $("<div class='filter'><label for='vnu_show_only_errors'>Show only errors</label><input type='checkbox' id='vnu_show_only_errors'/></div>");
    const vnuHideKnownIssuesFilter = $("<div class='filter'><label for='vnu_hide_known_issues'>Hide known issues</label><input type='checkbox' id='vnu_hide_known_issues'/></div>");
    vnuContainer.prepend(vnuShowOnlyErrorsFilter);
    vnuContainer.prepend(vnuHideKnownIssuesFilter);

    vnuShowOnlyErrorsFilter.change(function () {filterTableErrors('vnu_show_only_errors', vnuTable);});
    vnuHideKnownIssuesFilter.change(function () {filterTableKnownIssues('vnu_hide_known_issues', vnuTable);});

    function filterTableKnownIssues(elementId,table) {
        const hideKnownIssuesChecked = $(`input:checkbox[id=${elementId}]`).is(':checked');
        const searchValue = hideKnownIssuesChecked ? '^$' : '';
        const regexEnabled = true;
        const caseInsensitive = false;
        table.column(6).search(searchValue, regexEnabled, caseInsensitive).draw();
    }

    function filterTableErrors(elementId, table) {
        const toggleErrorsChecked = $(`input:checkbox[id=${elementId}]`).is(':checked');
        const searchValue = toggleErrorsChecked ? 'ERROR' : '';
        table.column(4).search(searchValue).draw();
    }

});