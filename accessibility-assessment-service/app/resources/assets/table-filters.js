$(document).ready(function () {
    const axeTable = $('#axeTable').DataTable(
        {
            pageLength: 100,
        },
    ),
    vnuTable = $('#vnuTable').DataTable(
        {
            pageLength: 100,
        },
    );

    function createCheckboxFilter(elementId, label) {
        return $(`<div class='filter'><label for='${elementId}'>${label}</label><input type='checkbox' id='${elementId}'/></div>`);
    }

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

    const axeSearchFilter = $("#axeTable_filter");
    const axeShowOnlyErrorsFilter = createCheckboxFilter('axe_show_only_errors', 'Show only errors');
    const axeHideKnownIssuesFilter = createCheckboxFilter('axe_hide_known_issues', 'Hide known issues');
    axeSearchFilter.prepend(axeShowOnlyErrorsFilter);
    axeSearchFilter.prepend(axeHideKnownIssuesFilter);

    axeShowOnlyErrorsFilter.change( (event) => filterTableErrors(event, axeTable));
    axeHideKnownIssuesFilter.change( (event) => filterTableKnownIssues(event, axeTable));

    const vnuSearchFilter = $("#vnuTable_filter");
    const vnuShowOnlyErrorsFilter = createCheckboxFilter('vnu_show_only_errors', 'Show only errors');
    const vnuHideKnownIssuesFilter = createCheckboxFilter('vnu_hide_known_issues', 'Hide known issues');
    vnuSearchFilter.prepend(vnuShowOnlyErrorsFilter);
    vnuSearchFilter.prepend(vnuHideKnownIssuesFilter);

    vnuShowOnlyErrorsFilter.change( (event) => filterTableErrors(event, vnuTable));
    vnuHideKnownIssuesFilter.change( (event) => filterTableKnownIssues(event, vnuTable));

});
