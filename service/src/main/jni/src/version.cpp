// Version number, extracted from git
#define QUOTE(str) #str
#define XQUOTE(str) QUOTE(str)
const char *kFCServerVersion = XQUOTE(FCSERVER_VERSION);
