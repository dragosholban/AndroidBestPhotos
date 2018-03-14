package dragosholban.com.bestphotos;

import java.util.List;

public class FacebookPhotos {

    public class Datum {

        public class Reactions {

            public class Datum_ {

                public String id;
                public String name;
                public String type;

            }

            public class Paging {

                public class Cursors {

                    public String before;
                    public String after;

                }

                public Cursors cursors;
                public String next;

            }

            public class Summary {

                public Integer total_count;
                public String viewer_reaction;

            }

            public List<Datum_> data = null;
            public Paging paging;
            public Summary summary;

        }

        public class Image {

            public Integer height;
            public String source;
            public Integer width;

        }

        public String picture;
        public Reactions reactions;
        public String link;
        public List<Image> images = null;
        public String created_time;
        public String id;

    }

    public class Paging_ {

        public class Cursors_ {

            public String before;
            public String after;

        }

        public Cursors_ cursors;
        public String next;

    }

    public List<Datum> data = null;
    public Paging_ paging;

}