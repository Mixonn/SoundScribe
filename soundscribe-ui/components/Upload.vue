<template>
  <div class="container">
    <form v-if="isInitial || isSaving" enctype="multipart/form-data" novalidate>
      <div class="dropbox">
        <input
          ref="file"
          type="file"
          multiple
          :disabled="isSaving"
          accept=".mp3,audio/*"
          class="input-file"
          @change="onSelect"
        >
        <p v-if="isInitial">
          Drag your file(s) here to begin<br> or click to browse
        </p>
        <p v-if="isSaving">
          Uploading {{ fileCount }} files...
        </p>
      </div>
    </form>

    <div v-if="isSuccess">
      <h2>Uploaded {{ uploadedFiles.length }} file(s) successfully.</h2>
      <p>
        <a href="javascript:void(0)" @click="reset()">Upload again</a>
      </p>
    </div>
    <!--FAILED-->
    <div v-if="isFailed">
      <h2>Uploaded failed.</h2>
      <p>
        <a href="javascript:void(0)" @click="reset()">Try again</a>
      </p>
      <pre>{{ uploadError }}</pre>
    </div>
  </div>
</template>

<script>
// https://scotch.io/tutorials/how-to-handle-file-uploads-in-vue-2

const STATUS_INITIAL = 0
const STATUS_SAVING = 1
const STATUS_SUCCESS = 2
const STATUS_FAILED = 3

export default {
  name: 'App',
  data () {
    return {
      uploadedFiles: [],
      uploadError: null,
      currentStatus: null
    }
  },
  computed: {
    isInitial () {
      return this.currentStatus === STATUS_INITIAL;
    },
    isSaving () {
      return this.currentStatus === STATUS_SAVING;
    },
    isSuccess () {
      return this.currentStatus === STATUS_SUCCESS;
    },
    isFailed () {
      return this.currentStatus === STATUS_FAILED;
    }
  },
  mounted () {
    this.reset();
  },
  methods: {
    upload (formData) {
      console.log(formData)
      const config = {
        headers: {
          ...formData.headers
        }
      }
      return this.$axios.post('/', formData, config)
        .then(x => x.data)
    },
    reset () {
      // reset form to initial state
      this.currentStatus = STATUS_INITIAL;
      this.uploadedFiles = [];
      this.uploadError = null;
    },
    save (formData) {
      // upload data to the server
      this.currentStatus = STATUS_SAVING;

      this.upload(formData)
        .then((x) => {
          this.uploadedFiles = [].concat(x);
          this.currentStatus = STATUS_SUCCESS;
        })
        .catch((err) => {
          console.debug(err)
          this.uploadError = err.response;
          this.currentStatus = STATUS_FAILED;
        });
    },
    onSelect () {
      const fileList = this.$refs.file.files
      this.fileCount = fileList.length
      // handle file changes
      const formData = new FormData();

      if (!fileList.length) {
        return;
      }

      // append the files to FormData
      Array
        .from(Array(fileList.length).keys())
        .map((x) => {
          formData.append('file', fileList[x], fileList[x].name);
        });

      // save it
      this.save(formData);
    }
  }
}
</script>

<style lang="scss">
  .container {
    align-content: center;
  }

  .dropbox {
    outline: 2px dashed grey; /* the dash box */
    outline-offset: -10px;
    background: white;
    color: black;
    padding: 10px 10px;
    min-width: 400px;
    min-height: 200px; /* minimum height */
    position: relative;
    cursor: pointer;
  }

  .input-file {
    opacity: 0; /* invisible but it's there! */
    width: 100%;
    height: 200px;
    position: absolute;
    cursor: pointer;
  }

  .dropbox:hover {
    background: floralwhite; /* when mouse over to the drop zone, change color */
  }

  .dropbox p {
    font-size: 1.2em;
    text-align: center;
    padding: 50px 0;
  }
</style>
