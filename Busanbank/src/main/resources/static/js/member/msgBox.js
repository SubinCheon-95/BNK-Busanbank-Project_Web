document.addEventListener("DOMContentLoaded", () => {
    const msgBox = document.getElementById("msgBox");
    if (msgBox) {
        const msg = msgBox.dataset.msg;
        if (msg) alert(msg);
    }
});
