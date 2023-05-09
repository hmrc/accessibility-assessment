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

    function createCheckboxFilter(elementId, label) {
        return $(`<div class='filter'><label for='${elementId}'>${label}</label><input type='checkbox' id='${elementId}'/></div>`);
    }

    const axeContainer = $("#axeTable_wrapper");
    const axeShowOnlyErrorsFilter = createCheckboxFilter('axe_show_only_errors', 'Show only errors');
    const axeHideKnownIssuesFilter = createCheckboxFilter('axe_hide_known_issues', 'Hide known issues');
    axeContainer.prepend(axeShowOnlyErrorsFilter);
    axeContainer.prepend(axeHideKnownIssuesFilter);

    axeShowOnlyErrorsFilter.change(function (event) {filterTableErrors(event, axeTable);});
    axeHideKnownIssuesFilter.change(function (event) { filterTableKnownIssues(event, axeTable);});

    const vnuContainer = $("#vnuTable_wrapper");
    const vnuShowOnlyErrorsFilter = createCheckboxFilter('vnu_show_only_errors', 'Show only errors');
    const vnuHideKnownIssuesFilter = createCheckboxFilter('vnu_hide_known_issues', 'Hide known issues');
    vnuContainer.prepend(vnuShowOnlyErrorsFilter);
    vnuContainer.prepend(vnuHideKnownIssuesFilter);

    vnuShowOnlyErrorsFilter.change(function (event) {filterTableErrors(event, vnuTable);});
    vnuHideKnownIssuesFilter.change(function (event) {filterTableKnownIssues(event, vnuTable);});

    function filterTableKnownIssues(event, table) {
        const hideKnownIssuesChecked = event.target.checked;
        const searchValue = hideKnownIssuesChecked ? '^$' : '';
        const regexEnabled = true;
        const caseInsensitive = false;
        table.column(6).search(searchValue, regexEnabled, caseInsensitive).draw();
    }

    function filterTableErrors(event, table) {
        const toggleErrorsChecked = event.target.checked;
        const searchValue = toggleErrorsChecked ? 'ERROR' : '';
        table.column(4).search(searchValue).draw();
    }

});