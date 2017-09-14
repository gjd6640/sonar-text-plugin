    if (itm.match(/^\d\d[\/-]\d\d[\/-]\d\d\s\d\d[:]\d\d$/)) sortfn = ts_sort_datetime;
    // This line has a character that isn't understood by UTF-8. The plugin should just ignore (not read) that character.
    if (itm.match(/^[£$]/)) sortfn = ts_sort_currency;
    if (itm.match(/^[\d\.]+$/)) sortfn = ts_sort_numeric;