Various note on CSS:
- Media query for different sizes of devices

    ```
    @media screen and (min-width: 1200px){} /* Desktop */
    @media screen and (min-width: 1025px) and (max-width: 1199px){} /* small laptop */
    @media screen and (min-width: 768px) and (max-width: 1024px){} /* tablet */
    @media screen and (min-width: 575px) and (max-width: 767.98px){} /* tablet and large mobiles */
    @media screen and (min-width: 320px) and (max-width: 480px){} /* Mobile*/
    ```

- A block-level element always takes up the full width available (stretches out to the left and right as far as it can).
- width (means fixed width) + "margin auto" - to horizontally center the element within its container.
    - The problem with the <div> above occurs when the browser window is smaller than the width of the element. The browser then adds a horizontal scrollbar to the page, since it's using fixed width.
    - width can be 100%, means the width equals to 100% of the parent element
- max-width : it states a maximum width , so if the browser window is smaller than the max-width of the element. The browser will adjust the element width. 
- margin-right : auto
    - The auto keyword will give **the right side a share of the remaining space.** When combined with margin-left: auto , it will center the element, if a fixed width is defined.
- float - specifies how an element should float, ie. left, right, none, inherit
    - **Normally div elements will be displayed on top of each other. However, if we use float: left we can let elements float next to each other**
    - And then we can use width as percentage value to control the width of each div with under different media type and features 
        - originally - 4 columns
        - 50% - 2 columns
        - 100% - 1 column

    ```
    <div class="row">
        <div class="column" style="background-color:#aaa;">
            <h2>Column 1</h2>
            <p>Some text..</p>
        </div>
        <div class="column" style="background-color:#bbb;">
            <h2>Column 2</h2>
            <p>Some text..</p>
        </div>
        <div class="column" style="background-color:#ccc;">
            <h2>Column 3</h2>
            <p>Some text..</p>
        </div>
        <div class="column" style="background-color:#ddd;">
            <h2>Column 4</h2>
            <p>Some text..</p>
        </div>
    </div>
    ```

    ```
    .column {
        float: left;
        width: 25%;
        padding: 20px;
    }

    @media screen and (max-width: 992px) {
        .column {
            width: 50%;
        }
    }

    /* On screens that are 600px wide or less, make the columns stack on top of each other instead of next to each other */

    @media screen and (max-width: 600px) {
        .column {
            width: 100%;
        }
    }
    ```

    - When we use the float property, and we want the next element below (not on right or left), we will have to use the `clear` property, eg. none, left,right,both,inherit
    - The clearfix Hack
    - If a floated element is taller than the containing element, it will "overflow" outside of its container. We can then add a clearfix hack to solve this problem:

    ```
    .clearfix {
        overflow: auto;
    }
    ```


- CSS Pseudo-elements
    - A CSS `pseudo-element` is used to style specified parts of an element.
        - **Style the first letter, or line, of an element**
        - **Insert content before, or after, the content of an element**
        
        ```
        selector::pseudo-element {
            property: value;
        }
        ```

        - `::first-line` Pseudo-element
            - The ::first-line pseudo-element is used to add a special style to the first line of a text.
            - The following example formats the first line of the text in all <p> elements:

            ```
            p::first-line {
                color: #ff0000;
                font-variant: small-caps;
            }
            ```
        - `::first-letter` pseudo-element is used to add a special style to the first letter of a text.
            - The example above will display the first letter of paragraphs with class="intro", in red and in a larger size.

            ```
            p.intro::first-letter {
              color: #ff0000;
              font-size: 200%;
            }
            ```
        - `::before` Pseudo-element
            - It can be used to insert some content before the content of an element.
            - The following example inserts an image before the content of each <h1> element:
            - similarly for `::after`

            ```
            h1::before {
                content: url(smiley.gif);
            }
            ```

            - For example used on a row of div

            ```
            .row:after {
                content: "";
                display: table;
                clear: both;
            }
            ```
        - `::marker` Pseudo-element
            - The ::marker pseudo-element selects the markers of list items, eg. used for ul/ol elements
        
        - `::selection` Pseudo-element
            - The ::selection pseudo-element matches the portion of an element that is **selected by a user.**
            - The following example makes the selected text red on a yellow background:

            ```
            ::selection {
                color: red;
                background: yellow;
            }
            ```
- CSS Attribute Selectors - Style HTML Elements With Specific Attributes
    - It is possible to style HTML elements that have specific attributes or attribute values.
        - CSS `[attribute]` Selector
            - The `[attribute]` selector is used to select elements with a specified attribute.
            - The following example selects all <a> elements with a target attribute:

            ```
            a[target] {
                background-color: yellow;
            }
            ```
        - CSS `[attribute="value"]` Selector
            - The `[attribute="value"]` selector is used to select elements with a specified attribute and value.
        - CSS `[attribute~="value"]` Selector
            - The `[attribute~="value"]` selector is used to select elements with an attribute value containing a **specified word.**
            - contains a space-separated list of words, one of which is "flower":

            ```
            [title~="flower"] {
                border: 5px solid yellow;
            }
            ```

        - CSS [attribute*=value] Selector
            - Set a background color on all <div> elements that have a class attribute **value containing "test"**:

            ```
            div[class*="test"] {
                background: #ffff00;
            }
            ```

            ```
            /* For desktop: */
            .col-1 {width: 8.33%;}
            .col-2 {width: 16.66%;}
            .col-3 {width: 25%;}
            .col-4 {width: 33.33%;}
            .col-5 {width: 41.66%;}
            .col-6 {width: 50%;}
            .col-7 {width: 58.33%;}
            .col-8 {width: 66.66%;}
            .col-9 {width: 75%;}
            .col-10 {width: 83.33%;}
            .col-11 {width: 91.66%;}
            .col-12 {width: 100%;}

            @media only screen and (max-width: 768px) {
                /* For mobile phones: */
                [class*="col-"] {
                    width: 100%;
                }
            }
            ```


- Media query for phone, tablet, pc

    ```
    /* For mobile phones: */
    [class*="col-"] {
    width: 100%;
    }

    @media only screen and (min-width: 600px) {
    /* For tablets: */
    .col-s-1 {width: 8.33%;}
    .col-s-2 {width: 16.66%;}
    .col-s-3 {width: 25%;}
    .col-s-4 {width: 33.33%;}
    .col-s-5 {width: 41.66%;}
    .col-s-6 {width: 50%;}
    .col-s-7 {width: 58.33%;}
    .col-s-8 {width: 66.66%;}
    .col-s-9 {width: 75%;}
    .col-s-10 {width: 83.33%;}
    .col-s-11 {width: 91.66%;}
    .col-s-12 {width: 100%;}
    }

    @media only screen and (min-width: 768px) {
    /* For desktop: */
    .col-1 {width: 8.33%;}
    .col-2 {width: 16.66%;}
    .col-3 {width: 25%;}
    .col-4 {width: 33.33%;}
    .col-5 {width: 41.66%;}
    .col-6 {width: 50%;}
    .col-7 {width: 58.33%;}
    .col-8 {width: 66.66%;}
    .col-9 {width: 75%;}
    .col-10 {width: 83.33%;}
    .col-11 {width: 91.66%;}
    .col-12 {width: 100%;}
    }
    ```
    - HTML Example

    ```
    <div class="row">
    <div class="col-3 col-s-3">...</div>
    <div class="col-6 col-s-9">...</div>
    <div class="col-3 col-s-12">...</div>
    </div>
    ```
    - For desktop:

        - The first and the third section will both span 3 columns each. The middle section will span 6 columns.

    - For tablets:

        - The first section will span 3 columns, the second will span 9, and the third section will be displayed below the first two sections, and it will span 12 columns: